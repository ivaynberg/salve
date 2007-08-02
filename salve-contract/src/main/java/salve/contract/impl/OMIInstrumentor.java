package salve.contract.impl;

import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;

public class OMIInstrumentor extends ClassAdapter {
	private String owner;
	private final OMIAnalyzer analyzer;

	public OMIInstrumentor(ClassVisitor cv, OMIAnalyzer analyzer) {
		super(cv);
		this.analyzer = analyzer;
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
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		if (analyzer.shouldInstrument(name, desc)) {
			return new MethodInstrumentor(mv, access, name, desc);
		}
		return mv;
	}

	private class MethodInstrumentor extends AbstractMethodInstrumentor {
		private int flag;

		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			if (isSuperCall(opcode, owner, name, desc)) {
				push(true);
				storeLocal(flag);
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		protected void onMethodEnter() {
			flag = newLocal(Type.BOOLEAN_TYPE);
			push(false);
			storeLocal(flag);
		}

		@Override
		protected void onMethodExit(int opcode) {
			if (opcode != ATHROW) {
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
					&& !OMIInstrumentor.this.owner.equals(owner);
		}

	}
}
