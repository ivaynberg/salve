package salve.contract.initonce;

import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.GeneratorAdapter;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;

public class ClassInstrumentor extends ClassAdapter {

	private class MethodInstrumentor extends MethodAdapter {
		private final GeneratorAdapter ge;

		public MethodInstrumentor(final MethodVisitor mv, int access, String name, String desc) {
			super(mv);
			ge = new GeneratorAdapter(mv, access, name, desc);
			super.mv = ge;
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			if (opcode == Opcodes.PUTFIELD) {
				if (analyzer.isInitOnce(owner, name)) {
					final Type clazz = Type.getObjectType(owner);
					final Type field = Type.getType(desc);
					final Type exception = Type.getObjectType("java/lang/IllegalStateException");

					Label accessGranted = new Label();
					ge.loadThis();
					ge.getField(clazz, name, field);
					ge.ifNull(accessGranted);

					ge.throwException(exception, "Trying to set value on a @InitOnce " + owner + "#" + name
							+ " field that is already set to a non-null value.");

					ge.visitLabel(accessGranted);
				}
			}
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}

	private final ClassAnalyzer analyzer;

	public ClassInstrumentor(ClassVisitor cv, ClassAnalyzer analyzer) {
		super(cv);
		this.analyzer = analyzer;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return new MethodInstrumentor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
	}

}
