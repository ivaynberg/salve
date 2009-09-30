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
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.model.CtMethod;
import salve.util.asm.AsmUtil;

public class NotNullInstrumentor extends AbstractMethodInstrumentor implements Constants {

	private final CtMethod method;

	public NotNullInstrumentor(MethodVisitor mv, String owner, int access, String name, String desc,
			InstrumentationContext ctx) {
		super(mv, owner, access, name, desc, ctx);
		method = ctx.getModel().getClass(owner).getMethod(name, desc);
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
		for (int i = 0; i < method.getArgCount(); i++) {
			if (method.getArgAnnot(i, NOTNULL.getDescriptor()) != null) {
				if (!checkType(method.getArgType(i))) {
					error("Annotation @%s cannot be applied to a primitive argument", NOTNULL.getClassName());
				}

				final Label end = new Label();
				loadArg(i);
				ifNonNull(end);
				throwIllegalArgumentException(method, i, "cannot be null");
				mark(end);
			}
		}
	}

	@Override
	protected void onMethodExit(int opcode) {
		if (method.getAnnot(NOTNULL.getDescriptor()) != null) {
			if (!checkType(method.getReturnType())) {
				error("Annotation @%s cannot be applied to a method with a primitive or void return type", NOTNULL
						.getClassName());
			}

			if (opcode == ARETURN) {
				String msg = "Method `";
				msg += getMethodDefinitionString();
				msg += "` cannot return a null value";

				Label end = new Label();
				dup();
				ifNonNull(end);
				throwIllegalStateException(msg);
				mark(end);
				returnValue();
			}

		}
	}

}
