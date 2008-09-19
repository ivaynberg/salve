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

import java.math.BigInteger;

import salve.InstrumentorMonitor;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Label;
import salve.asmlib.Method;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.contract.IllegalAnnotationUseException;
import salve.util.asm.AsmUtil;

public class NumericalInstrumentor extends AbstractMethodInstrumentor {
	private final Label methodStart = new Label();
	private final Label paramsCheck = new Label();
	private final Label returnValueCheck = new Label();

	private int[] argannots = null;
	private int annot;

	public NumericalInstrumentor(MethodVisitor mv, InstrumentorMonitor monitor, String owner, int access, String name,
			String desc) {
		super(mv, monitor, owner, access, name, desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		final int mode = descToMode(desc);
		if (mode > 0) {

			if (!acceptType(getReturnType())) {
				throw new IllegalAnnotationUseException("Cannot use annotation " + Type.getType(desc).getClassName()
						+ " on method " + getMethodDefinitionString());
			}

			if (annot > 0 && annot != mode) {
				// FIXME message
				throw new IllegalAnnotationUseException("...");
			}
			annot = mode;
			return null;
		} else {
			return mv.visitAnnotation(desc, visible);
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		if (argannots != null || annot > 0) {
			getMonitor().methodModified(getOwner(), getMethodAccess(), getMethodName(), getMethodDesc());
		}
		if (argannots != null) {
			mark(paramsCheck);
			for (int i = 0; i < argannots.length; i++) {
				if (argannots[i] > 0) {
					final int mode = argannots[i];
					final Type type = getParamType(i);
					final Label end = new Label();
					loadArg(i);
					if (!AsmUtil.isPrimitive(type)) {
						// null check of non-primitive type before value check
						dup(type);
						Label notnull = new Label();
						ifNonNull(notnull);
						throwIllegalArgumentException(i, "cannot be null");
						mark(notnull);

					}
					if (AsmUtil.isBigInteger(type)) {
						// Compare to BigInteger.ZERO.
						compareToZero();
						// Check return value set by compareToZero().
						checkValue(mode, Type.INT_TYPE, end);
					} else {
						// Use the default check.
						checkValue(mode, type, end);
					}
					// FIXME message
					throwIllegalArgumentException(i, modeToErrorString(mode));
					mark(end);

				}
			}
			goTo(methodStart);

		}

		if (annot > 0) {

			// FIXME message
			String msg = "Method returned invalid value";
			Label end = new Label();
			final Type type = getReturnType();
			mark(returnValueCheck);
			dup(type);
			if (!AsmUtil.isPrimitive(type)) {
				// null check of non-primitive type before value check
				dup(type);
				Label notnull = new Label();
				ifNonNull(notnull);
				throwIllegalStateException("Method " + getMethodDefinitionString() + " cannot return null");
				mark(notnull);
			}
			if (AsmUtil.isBigInteger(type)) {
				// Compare to BigInteger.ZERO.
				compareToZero();
				// Check return value set by compareToZero().
				checkValue(annot, Type.INT_TYPE, end);
			} else {
				// Use the default check.
				checkValue(annot, getReturnType(), end);
			}
			throwIllegalStateException(msg);
			mark(end);
			returnValue();
		}

		mv.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {

		final int mode = descToMode(desc);
		if (mode > 0) {
			Type paramType = getParamType(parameter);
			if (!acceptType(paramType)) {
				throw new IllegalAnnotationUseException("Cannot use annotation " + Type.getType(desc).getClassName()
						+ " on argument of type " + paramType.getClassName() + " in method "
						+ getMethodDefinitionString());
			}

			if (argannots == null) {
				argannots = new int[getParamCount()];
			}
			if (argannots[parameter] > 0 && argannots[parameter] != mode) {
				// FIXME message
				throw new IllegalAnnotationUseException("...");
			}
			argannots[parameter] = mode;
			return null;
		} else {
			return mv.visitParameterAnnotation(parameter, desc, visible);
		}

	}

	@Override
	protected void onMethodEnter() {
		if (argannots != null) {
			goTo(paramsCheck);
			mark(methodStart);
		}
	}

	@Override
	protected void onMethodExit(int opcode) {
		if (annot > 0 && opcode != ATHROW) {
			goTo(returnValueCheck);
		}
	}

	private boolean acceptType(Type type) {
		return AsmUtil.isDouble(type) || AsmUtil.isFloat(type) || AsmUtil.isLong(type) || AsmUtil.isInteger(type)
				|| AsmUtil.isShort(type) || AsmUtil.isByte(type) || AsmUtil.isBigInteger(type);
	}

	private void checkValue(int mode, final Type type, Label end) {
		Type primitive = AsmUtil.toPrimitive(type);
		if (!AsmUtil.isPrimitive(type)) {
			unbox(primitive);
		}

		switch (primitive.getSort()) {
			case Type.DOUBLE:
				visitInsn(DCONST_0);
				switch (mode) {
					case GT:
					case GE:
						visitInsn(DCMPG);
						break;
					case LT:
					case LE:
						visitInsn(DCMPL);
						break;
				}
				break;
			case Type.FLOAT:
				visitInsn(FCONST_0);
				switch (mode) {
					case GT:
					case GE:
						visitInsn(FCMPG);
						break;
					case LT:
					case LE:
						visitInsn(FCMPL);
						break;
				}
				break;
			case Type.LONG:
				visitInsn(LCONST_0);
				visitInsn(LCMP);
				break;
		}
		visitJumpInsn(mode, end);

	}

	// The BigInteger should be loaded already.
	private void compareToZero() {
		// Generate the BigInteger.compareTo(BigInteger.ZERO).
		Type bigInt = Type.getType(BigInteger.class);
		getStatic(bigInt, "ZERO", bigInt);
		invokeVirtual(bigInt, Method.getMethod("int compareTo (java.math.BigInteger)"));
	}

	private int descToMode(String desc) {
		if (GT0.getDescriptor().equals(desc)) {
			return GT;
		} else if (GE0.getDescriptor().equals(desc)) {
			return GE;
		} else if (LT0.getDescriptor().equals(desc)) {
			return LT;
		} else if (LE0.getDescriptor().equals(desc)) {
			return LE;
		}
		return 0;
	}

	private void dup(Type type) {
		if (type.getSize() == 2) {
			dup2();
		} else {
			dup();
		}
	}

	private String modeToErrorString(int mode) {
		switch (mode) {
			case GT:
				return "cannot be less then or equal to zero";
			case GE:
				return "cannot be less then zero";
			case LT:
				return "cannot be greater then or equal to zero";
			case LE:
				return "cannot be greater then zero";
			default:
				throw new IllegalStateException("Unknown mode");
		}
	}

}
