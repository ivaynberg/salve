package salve.contract.impl;

import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;

public class NotNullInstrumentor extends ClassAdapter {
	private String owner;

	public NotNullInstrumentor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		owner = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return new MethodInstrumentor(cv.visitMethod(access, name, desc,
				signature, exceptions), access, name, desc);
	}

	private class MethodInstrumentor extends AbstractMethodInstrumentor
			implements Constants {

		private boolean notNull = false;

		private final Label methodStart = new Label();
		private final Label paramsCheck = new Label();
		private final Label returnValueCheck = new Label();

		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (NOTNULL_DESC.equals(desc)) {
				notNull = true;
				return null;
			}
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			{
				mark(paramsCheck);
				for (Parameter arg : getParameters()) {
					final Label end = new Label();
					loadArg(arg.index);
					ifNonNull(end);
					throwIllegalArgumentException(arg);
					mark(end);
				}
				goTo(methodStart);
			}

			if (notNull) {
				String msg = "Method `";
				// TODO better method name
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
		public AnnotationVisitor visitParameterAnnotation(int parameter,
				String desc, boolean visible) {
			int index = parameter;
			if (NOTNULL_DESC.equals(desc)) {
				getOrCreateParameter(index);
				return null;
			}
			return super.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		protected void onMethodEnter() {
			goTo(paramsCheck);
			mark(methodStart);
		}

		@Override
		protected void onMethodExit(int opcode) {
			if (opcode == ARETURN) {
				goTo(returnValueCheck);
			}
		}

	}

}
