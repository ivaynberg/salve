package salve.util.asm;

import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Opcodes;
import salve.org.objectweb.asm.Type;

public class GeneratorAdapter extends salve.org.objectweb.asm.commons.GeneratorAdapter implements Opcodes {

	public GeneratorAdapter(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
	}

	@Override
	public void push(Type value) {
		if (value == null) {
			mv.visitInsn(Opcodes.ACONST_NULL);
		} else {
			int sort = value.getSort();
			if (sort == Type.BOOLEAN) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
			} else if (sort == Type.CHAR) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Char", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.BYTE) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.SHORT) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.INT) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.FLOAT) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.LONG) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.DOUBLE) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
			} else {
				mv.visitLdcInsn(value);
			}

		}
	}
}
