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

public class NotEmptyInstrumentor extends AbstractMethodInstrumentor implements Constants {

	private final CtMethod method;

	public NotEmptyInstrumentor(MethodVisitor mv, String owner, int access, String name, String desc,
			InstrumentationContext ctx) {
		super(mv, owner, access, name, desc, ctx);
		method = ctx.getModel().getClass(owner).getMethod(name, desc);
	}

	@Override
	protected void onMethodEnter() {
		for (int i = 0; i < method.getArgCount(); i++) {
			if (method.getArgAnnot(i, NOTEMPTY.getDescriptor()) != null) {

				final Type type = method.getArgType(i);

				if (!STRING_TYPE.equals(type)) {
					error("Parameter annotation @%s can only be applied on parameters of type %s", NOTEMPTY
							.getClassName(), STRING_TYPE.getClassName());
					return;
				}

				final Label end = new Label();
				final Label checkEmpty = new Label();
				loadArg(i);
				ifNonNull(checkEmpty);
				throwIllegalArgumentException(method, i, "cannot be null");
				mark(checkEmpty);
				loadArg(i);
				invokeVirtual(STRING_TYPE, STRING_TRIM_METHOD);
				invokeVirtual(STRING_TYPE, STRING_LENGTH_METHOD);
				ifZCmp(NE, end);
				throwIllegalArgumentException(method, i, "cannot be empty");
				mark(end);
			}
		}
	}

	@Override
	protected void onMethodExit(int opcode) {
		if (method.getAnnot(NOTEMPTY.getDescriptor()) != null) {

			if (!STRING_TYPE.equals(method.getReturnType())) {
				error("Annotation @%s can only be applied on methods with return type %s", NOTEMPTY.getClassName(),
						STRING_TYPE.getClassName());
				return;
			}

			if (opcode == ARETURN) {
				final String nullMsg = "Method `" + getMethodDefinitionString() + "` cannot return a null value";
				final String emptyMsg = "Method `" + getMethodDefinitionString() + " cannot return an empty string";

				final Label end = new Label();
				final Label checkEmpty = new Label();

				dup();
				ifNonNull(checkEmpty);
				throwIllegalStateException(nullMsg);
				mark(checkEmpty);
				dup();
				invokeVirtual(STRING_TYPE, STRING_TRIM_METHOD);
				invokeVirtual(STRING_TYPE, STRING_LENGTH_METHOD);
				ifZCmp(NE, end);
				throwIllegalStateException(emptyMsg);
				mark(end);
				returnValue();
			}
		}
	}

}
