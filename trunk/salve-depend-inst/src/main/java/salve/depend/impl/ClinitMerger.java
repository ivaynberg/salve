package salve.depend.impl;

import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;

class ClinitMerger extends MethodAdapter implements Opcodes {

	public ClinitMerger(MethodVisitor mv) {
		super(mv);
	}

	@Override
	public void visitCode() {
	}

	@Override
	public void visitEnd() {
	}

	@Override
	public void visitInsn(int opcode) {
		if (opcode != RETURN) {
			mv.visitInsn(opcode);
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {

	}
}
