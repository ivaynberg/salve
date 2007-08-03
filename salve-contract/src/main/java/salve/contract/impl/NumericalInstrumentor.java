package salve.contract.impl;

import salve.contract.IllegalAnnotationUseException;
import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;
import salve.util.asm.AsmUtil;

public class NumericalInstrumentor extends ClassAdapter {

	public NumericalInstrumentor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new MethodInstrumentor(mv, access, name, desc);
	}

	private static class MethodInstrumentor extends AbstractMethodInstrumentor {
		private final Label methodStart = new Label();
		private final Label paramsCheck = new Label();
		private final Label returnValueCheck = new Label();

		private int[] argannots;
		private int annot;

		public MethodInstrumentor(MethodVisitor mv, int access, String name, String desc) {
			super(mv, access, name, desc);
		};

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			int mode = descToMode(desc);
			if (mode > 0) {

				if (!acceptType(getReturnType())) {
					throw new IllegalAnnotationUseException("Cannot use annotation "
							+ Type.getType(desc).getClassName() + " on method " + getMethodDefinitionString());
				}

				if (annot > 0 && annot != mode) {
					// FIXME message
					throw new IllegalAnnotationUseException("...");
				}
				annot = mode;
				return null;
			}
			return mv.visitAnnotation(desc, visible);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			if (argannots != null) {
				mark(paramsCheck);
				for (int i = 0; i < argannots.length; i++) {
					if (argannots[i] > 0) {

						final int mode = argannots[i];
						final Type type = getParamType(i);

						final String md = getMethodDefinitionString();

						loadArg(i);
						final Label end = new Label();
						checkValue(mode, type, end);
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
				mark(returnValueCheck);
				if (getReturnType().getSize() == 2) {
					dup2();
				} else {
					dup();
				}
				checkValue(annot, getReturnType(), end);
				throwIllegalStateException(msg);
				mark(end);
				returnValue();
			}

			super.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {

			int mode = descToMode(desc);
			if (mode > 0) {

				Type paramType = getParamType(parameter);
				if (!acceptType(paramType)) {
					throw new IllegalAnnotationUseException("Cannot use annotation "
							+ Type.getType(desc).getClassName() + " on argument of type " + paramType.getClassName()
							+ " in method " + getMethodDefinitionString());
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
			}
			return mv.visitAnnotation(desc, visible);
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
					|| AsmUtil.isShort(type) || AsmUtil.isByte(type);
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
}
