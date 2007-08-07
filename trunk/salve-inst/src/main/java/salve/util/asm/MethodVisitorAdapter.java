package salve.util.asm;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;

public class MethodVisitorAdapter implements MethodVisitor {

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

		return null;
	}

	public AnnotationVisitor visitAnnotationDefault() {

		return null;
	}

	public void visitAttribute(Attribute attr) {

	}

	public void visitCode() {

	}

	public void visitEnd() {

	}

	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {

	}

	public void visitFrame(int type, int local, Object[] local2, int stack,
			Object[] stack2) {

	}

	public void visitIincInsn(int var, int increment) {

	}

	public void visitInsn(int opcode) {

	}

	public void visitIntInsn(int opcode, int operand) {

	}

	public void visitJumpInsn(int opcode, Label label) {

	}

	public void visitLabel(Label label) {

	}

	public void visitLdcInsn(Object cst) {

	}

	public void visitLineNumber(int line, Label start) {

	}

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {

	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

	}

	public void visitMaxs(int maxStack, int maxLocals) {

	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {

	}

	public void visitMultiANewArrayInsn(String desc, int dims) {

	}

	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible) {

		return null;
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {

	}

	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {

	}

	public void visitTypeInsn(int opcode, String desc) {

	}

	public void visitVarInsn(int opcode, int var) {

	}

}
