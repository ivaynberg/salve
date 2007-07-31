package salve.spring.txn;

import salve.org.objectweb.asm.MethodAdapter;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Opcodes;

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
