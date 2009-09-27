/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.contract.impl;

import salve.InstrumentationContext;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.util.asm.AsmUtil;

public class NotNullInstrumentor extends AbstractMethodInstrumentor implements Constants {

	private boolean notNull = false;

	private final Label methodStart = new Label();
	private final Label paramsCheck = new Label();
	private final Label returnValueCheck = new Label();
	private boolean[] annotatedParams;

	public NotNullInstrumentor(MethodVisitor mv, String owner, int access, String name, String desc,
			InstrumentationContext ctx) {
		super(mv, owner, access, name, desc, ctx);
	}

	private boolean checkType(final Type ret) {
		if (AsmUtil.isPrimitive(ret)) {
			return false;
		} else if (ret.getSort() == Type.VOID) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void onMethodEnter() {
		if (annotatedParams != null) {
			goTo(paramsCheck);
			mark(methodStart);
		}
	}

	@Override
	protected void onMethodExit(int opcode) {
		if (notNull && opcode == ARETURN) {
			goTo(returnValueCheck);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (NOTNULL.getDescriptor().equals(desc)) {
			final Type ret = getReturnType();
			if (!checkType(ret)) {
				error("Annotation @%s cannot be applied to a method with a primitive or void return type", NOTNULL
						.getClassName());
				return null;
			}
			notNull = true;
			return null;
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		if (annotatedParams != null) {
			mark(paramsCheck);
			for (int i = 0; i < annotatedParams.length; i++) {
				if (annotatedParams[i]) {
					final Label end = new Label();
					loadArg(i);
					ifNonNull(end);
					throwIllegalArgumentException(i, "cannot be null");
					mark(end);
				}
			}
			goTo(methodStart);
		}

		if (notNull) {
			String msg = "Method `";
			msg += getMethodDefinitionString();
			msg += "` cannot return a null value";

			Label end = new Label();
			mark(returnValueCheck);
			dup();
			ifNonNull(end);
			throwIllegalStateException(msg);
			mark(end);
			returnValue();
		}

		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		if (NOTNULL.getDescriptor().equals(desc)) {
			if (!checkType(getParamType(parameter))) {
				error("Annotation @%s cannot be applied to a primitive argument", NOTNULL.getClassName());
			}
			if (annotatedParams == null) {
				annotatedParams = new boolean[getParamCount()];
			}
			annotatedParams[parameter] = true;
			return null;
		}
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

}
