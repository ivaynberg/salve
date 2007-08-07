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

import salve.InstrumentorMonitor;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.contract.IllegalAnnotationUseException;
import salve.util.asm.AsmUtil;

public class NotNullInstrumentor extends AbstractMethodInstrumentor implements Constants {

	private boolean notNull = false;

	private final Label methodStart = new Label();
	private final Label paramsCheck = new Label();
	private final Label returnValueCheck = new Label();
	private boolean[] annotatedParams;

	public NotNullInstrumentor(MethodVisitor mv, InstrumentorMonitor monitor, String owner, int access, String name,
			String desc) {
		super(mv, monitor, owner, access, name, desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (NOTNULL.getDescriptor().equals(desc)) {
			final Type ret = getReturnType();
			if (!checkType(ret)) {
				throw new IllegalAnnotationUseException("Annotation " + NOTNULL.getClassName()
						+ " cannot be applied to a method with a primitive or void return types");
			}
			notNull = true;
			return null;
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		if (annotatedParams != null || notNull) {
			getMonitor().methodModified(getOwner(), getMethodAccess(), getMethodName(), getMethodDesc());
		}
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
				throw new IllegalAnnotationUseException("Annotation " + NOTNULL.getClassName()
						+ " cannot be applied to a primitive argument");
			}
			if (annotatedParams == null) {
				annotatedParams = new boolean[getParamCount()];
			}
			annotatedParams[parameter] = true;
			return null;
		}
		return super.visitParameterAnnotation(parameter, desc, visible);
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

	private boolean checkType(final Type ret) {
		return !AsmUtil.isPrimitive(ret) & ret.getSort() != Type.VOID;
	}

}
