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

public class NotEmptyInstrumentor extends AbstractMethodInstrumentor implements Constants {

	private boolean notEmpty = false;

	private final Label methodStart = new Label();
	private final Label paramsCheck = new Label();
	private final Label returnValueCheck = new Label();
	private boolean[] annotatedParams;

	public NotEmptyInstrumentor(MethodVisitor mv, InstrumentorMonitor monitor, String owner, int access, String name,
			String desc) {
		super(mv, monitor, owner, access, name, desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (NOTEMPTY.getDescriptor().equals(desc)) {
			final Type type = getReturnType();
			if (!STRING_TYPE.equals(type)) {
				throw new IllegalAnnotationUseException("Annotation " + desc
						+ " can only be applied on methods with return type " + STRING_TYPE.getClassName());

			}
			notEmpty = true;
			return null;
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		if (annotatedParams != null || notEmpty) {
			getMonitor().methodModified(getOwner(), getMethodAccess(), getMethodName(), getMethodDesc());
		}

		if (annotatedParams != null) {
			mark(paramsCheck);
			for (int i = 0; i < annotatedParams.length; i++) {
				if (annotatedParams[i]) {
					final Label end = new Label();
					final Label checkEmpty = new Label();
					loadArg(i);
					ifNonNull(checkEmpty);
					throwIllegalArgumentException(i, "cannot be null");
					mark(checkEmpty);
					loadArg(i);
					invokeVirtual(STRING_TYPE, STRING_TRIM_METHOD);
					invokeVirtual(STRING_TYPE, STRING_LENGTH_METHOD);
					ifZCmp(NE, end);
					throwIllegalArgumentException(i, "cannot be empty");
					mark(end);
				}
			}
			goTo(methodStart);
		}

		if (notEmpty) {
			String nullMsg = "Method `" + getMethodDefinitionString() + "` cannot return a null value";
			String emptyMsg = "Method `" + getMethodDefinitionString() + " cannot return an empty string";

			final Label end = new Label();
			final Label checkEmpty = new Label();
			mark(returnValueCheck);
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

		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		if (NOTEMPTY.getDescriptor().equals(desc)) {
			final Type type = getParamType(parameter);
			if (!STRING_TYPE.equals(type)) {
				throw new IllegalAnnotationUseException("Annotation " + desc
						+ " can only be applied to arguments of type " + STRING_TYPE.getClassName());

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
		if (notEmpty && opcode == ARETURN) {
			goTo(returnValueCheck);
		}
	}

}
