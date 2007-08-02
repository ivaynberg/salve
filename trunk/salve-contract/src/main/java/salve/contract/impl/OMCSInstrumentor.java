package salve.contract.impl;

import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;

public class OMCSInstrumentor extends ClassAdapter {
	private String owner;

	public OMCSInstrumentor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		// TODO Auto-generated method stub
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	private class MethodInstrumentor extends AbstractMethodInstrumentor {
		private boolean instrument = false;
		private int flag;

		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (OMCS.getDescriptor().equals(desc)) {
				instrument = true;
				return null;
			}
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			if (instrument) {
				if (isSuperCall(opcode, owner, name, desc)) {
					push(true);
					storeLocal(flag);
				}
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		protected void onMethodEnter() {
			if (instrument) {
				flag = newLocal(Type.BOOLEAN_TYPE);
				push(false);
				storeLocal(flag);
			}
		}

		@Override
		protected void onMethodExit(int opcode) {
			if (instrument && opcode != ATHROW) {
				Label ok = new Label();
				loadLocal(flag);
				ifZCmp(NE, ok);
				throwIllegalStateException("This method did not invoke super implementation before returning");
				mark(ok);
			}
		}

		private boolean isSuperCall(int opcode, String owner, String name,
				String desc) {
			return opcode == INVOKESPECIAL && getMethodName().equals(name)
					&& getMethodDesc().equals(desc)
					&& !OMCSInstrumentor.this.owner.equals(owner);
		}

	}
}
