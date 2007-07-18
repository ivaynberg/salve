/**
 * 
 */
package salve.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import salve.asm.util.AsmUtil;
import salve.dependency.DependencyLibrary;
import salve.dependency.Key;
import salve.dependency.KeyImpl;
import salve.dependency.impl.DependencyConstants;

class DependencyInstrumentor extends ClassAdapter implements Opcodes {
	private final DependencyAnalyzer identifier;
	private boolean seenClinit = false;
	private String subject = null;

	public DependencyInstrumentor(ClassVisitor cv, DependencyAnalyzer identifier) {
		super(cv);
		this.identifier = identifier;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		subject = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public void visitEnd() {
		if (!seenClinit) {
			generateClInit();
		}
		super.visitEnd();
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		DependencyField field = identifier.getDependencyForField(name);
		if (field == null) {
			return super.visitField(access, name, desc, signature, value);
		} else {
			return super.visitField(ACC_PUBLIC + ACC_STATIC + ACC_FINAL,
					DependencyConstants.KEY_FIELD_PREFIX + name, Type
							.getDescriptor(Key.class), null, null);
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		if (AsmUtil.isClInitMethod(access, name, desc)) {
			seenClinit = true;
			return new ClInitInstrumentor(mv);
		} else {
			return new MethodInstrumentor(access, desc, mv);
		}
	}

	private void generateClInit() {
		MethodVisitor mvv = null;
		mvv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		ClInitInstrumentor mv = new ClInitInstrumentor(mvv);
		mv.visitCode();
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private class ClInitInstrumentor extends MethodAdapter {

		public ClInitInstrumentor(MethodVisitor mv) {
			super(mv);
		}

		@Override
		public void visitCode() {
			final String keyImplName = Type.getType(KeyImpl.class)
					.getInternalName();

			for (DependencyField field : identifier.getDependencies()) {
				final String fieldName = DependencyConstants.KEY_FIELD_PREFIX
						+ field.getName();

				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitTypeInsn(NEW, keyImplName);
				mv.visitInsn(DUP);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLdcInsn(Type.getType(field.getDesc()));
				mv.visitLdcInsn(Type.getObjectType(subject));
				Label l2 = new Label();
				mv.visitLabel(l2);
				mv.visitLdcInsn(fieldName);
				Label l3 = new Label();
				mv.visitLabel(l3);
				mv
						.visitMethodInsn(INVOKESPECIAL, keyImplName, "<init>",
								"(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V");
				mv.visitFieldInsn(PUTSTATIC, subject, fieldName,
						"Lsalve/dependency/Key;");
			}

			super.visitCode();
		}
	}

	private class MethodInstrumentor extends MethodAdapter implements Opcodes {
		private final LocalVariablesSorter lvs;

		private Map<DependencyField, Integer> fieldToLocal;

		public MethodInstrumentor(int acc, String desc, MethodVisitor mv) {
			super(mv);
			lvs = new LocalVariablesSorter(acc, desc, mv);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
				String desc) {
			DependencyField field = identifier.getDependencyForField(name);
			if (field != null) {
				if (fieldToLocal == null) {
					fieldToLocal = new HashMap<DependencyField, Integer>();
				}

				Integer local = fieldToLocal.get(field);
				if (local == null) {
					local = new Integer(lvs.newLocal(Type.getType(field
							.getDesc())));
					Label l0 = new Label();
					visitLabel(l0);
					visitFieldInsn(GETSTATIC, field.getOwner(),
							DependencyConstants.KEY_FIELD_PREFIX
									+ field.getName(), Type
									.getDescriptor(Key.class));
					visitMethodInsn(INVOKESTATIC, Type
							.getInternalName(DependencyLibrary.class),
							"locate",
							"(Lsalve/dependency/Key;)Ljava/lang/Object;");
					Label l1 = new Label();
					visitLabel(l1);
					visitTypeInsn(CHECKCAST, Type.getType(field.getDesc())
							.getInternalName());
					visitVarInsn(ASTORE, local.intValue());
					Label l2 = new Label();
					visitLabel(l2);
					visitVarInsn(ALOAD, local.intValue());
					fieldToLocal.put(field, local);
				} else {
					visitVarInsn(ALOAD, local.intValue());
				}
			} else {
				super.visitFieldInsn(opcode, owner, name, desc);
			}
		}

		@Override
		public void visitFrame(int type, int local, Object[] local2, int stack,
				Object[] stack2) {
			lvs.visitFrame(type, local, local2, stack, stack2);
		}

		@Override
		public void visitIincInsn(int var, int increment) {
			lvs.visitIincInsn(var, increment);
		}

		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			lvs.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			lvs.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			lvs.visitVarInsn(opcode, var);
		}

	}
}