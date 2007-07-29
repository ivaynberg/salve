/**
 * 
 */
package salve.dependency.impl;

import java.util.HashMap;
import java.util.Map;

import salve.asm.util.AsmUtil;
import salve.dependency.DependencyLibrary;
import salve.dependency.IllegalFieldWriteException;
import salve.dependency.InjectionStrategy;
import salve.dependency.Key;
import salve.dependency.KeyImpl;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.FieldVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodAdapter;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Opcodes;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.LocalVariablesSorter;

public class DependencyInstrumentorAdapter extends ClassAdapter implements
		Opcodes {
	private final DependencyAnalyzer locator;
	private boolean seenClinit = false;
	private String owner = null;

	public DependencyInstrumentorAdapter(ClassVisitor cv,
			DependencyAnalyzer locator) {
		super(cv);
		this.locator = locator;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		owner = name;
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
		DependencyField field = locator.locateField(owner, name);
		if (field == null) {
			return super.visitField(access, name, desc, signature, value);
		} else {
			if (field.getStrategy().equals(InjectionStrategy.REMOVE_FIELD)) {
				// TODO copy annots
				return new DependencyAnnotRemover(super.visitField(ACC_PUBLIC
						+ ACC_STATIC + ACC_FINAL,
						DependencyConstants.KEY_FIELD_PREFIX + name, Type
								.getDescriptor(Key.class), null, null));
			} else {
				Type fieldOwnerType = Type.getObjectType(field.getOwner());
				Type fieldType = Type.getType(field.getDesc());

				MethodVisitor mv = cv.visitMethod(ACC_PRIVATE,
						DependencyConstants.LOCATOR_METHOD_PREFIX + name,
						"()V", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, fieldOwnerType.getInternalName(),
						field.getName(), field.getDesc());
				Label l1 = new Label();
				mv.visitJumpInsn(IFNONNULL, l1);
				Label l2 = new Label();
				mv.visitLabel(l2);
				mv.visitTypeInsn(NEW, "salve/dependency/KeyImpl");
				mv.visitInsn(DUP);
				mv.visitLdcInsn(Type.getType(field.getDesc()));
				Label l3 = new Label();
				mv.visitLabel(l3);
				mv.visitLdcInsn(Type.getType(fieldOwnerType.getDescriptor()));
				mv.visitLdcInsn(field.getName());
				Label l4 = new Label();
				mv.visitLabel(l4);
				mv
						.visitMethodInsn(INVOKESPECIAL,
								"salve/dependency/KeyImpl", "<init>",
								"(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V");
				mv.visitVarInsn(ASTORE, 1);
				Label l5 = new Label();
				mv.visitLabel(l5);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKESTATIC,
						"salve/dependency/DependencyLibrary", "locate",
						"(Lsalve/dependency/Key;)Ljava/lang/Object;");
				mv.visitTypeInsn(CHECKCAST, fieldType.getInternalName());
				mv.visitFieldInsn(PUTFIELD, fieldOwnerType.getInternalName(),
						field.getName(), field.getDesc());
				mv.visitLabel(l1);
				mv.visitInsn(RETURN);
				Label l6 = new Label();
				mv.visitLabel(l6);
				mv.visitLocalVariable("this", fieldOwnerType.getDescriptor(),
						null, l0, l6, 0);
				mv.visitLocalVariable("key", "Lsalve/dependency/Key;", null,
						l5, l1, 1);
				mv.visitMaxs(5, 2);
				mv.visitEnd();
				return new DependencyAnnotRemover(super.visitField(access,
						name, desc, signature, value));
			}
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
			if (name.startsWith(DependencyConstants.LOCATOR_METHOD_PREFIX)) {
				return mv;
			} else {
				MethodInstrumentor inst = new MethodInstrumentor(access, desc,
						mv);
				inst.lvs = new LocalVariablesSorter(access, desc, inst);
				return inst.lvs;
			}
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

			for (DependencyField field : locator.locateFields(owner)) {
				if (InjectionStrategy.INJECT_FIELD.equals(field.getStrategy())) {
					continue;
				}
				final String fieldName = DependencyConstants.KEY_FIELD_PREFIX
						+ field.getName();

				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitTypeInsn(NEW, keyImplName);
				mv.visitInsn(DUP);
				Label l1 = new Label();
				mv.visitLabel(l1);
				mv.visitLdcInsn(Type.getType(field.getDesc()));
				mv.visitLdcInsn(Type.getObjectType(owner));
				Label l2 = new Label();
				mv.visitLabel(l2);
				mv.visitLdcInsn(fieldName);
				Label l3 = new Label();
				mv.visitLabel(l3);
				mv
						.visitMethodInsn(INVOKESPECIAL, keyImplName, "<init>",
								"(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V");
				mv.visitFieldInsn(PUTSTATIC, owner, fieldName,
						"Lsalve/dependency/Key;");
			}

			super.visitCode();
		}
	}

	private class MethodInstrumentor extends MethodAdapter implements Opcodes {
		public LocalVariablesSorter lvs;

		private Map<DependencyField, Integer> fieldToLocal;

		public MethodInstrumentor(int acc, String desc, MethodVisitor mv) {
			super(mv);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name,
				String desc) {

			if (opcode == GETSTATIC || opcode == PUTSTATIC) {
				super.visitFieldInsn(opcode, owner, name, desc);
				return;
			}

			DependencyField field = locator.locateField(owner, name);
			if (field != null) {

				if (opcode == PUTFIELD) {
					// throw IllegalFieldWriteException
					Label l0 = new Label();
					visitLabel(l0);
					visitTypeInsn(NEW,
							"salve/dependency/IllegalFieldWriteException");
					visitInsn(DUP);
					visitLdcInsn(field.getOwner().replace("/", "."));
					visitLdcInsn(field.getName());
					visitMethodInsn(INVOKESPECIAL, Type
							.getInternalName(IllegalFieldWriteException.class),
							"<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
					visitInsn(ATHROW);
					return;
				}

				if (fieldToLocal == null) {
					fieldToLocal = new HashMap<DependencyField, Integer>();
				}

				Integer local = fieldToLocal.get(field);

				if (field.getStrategy().equals(InjectionStrategy.REMOVE_FIELD)) {
					if (local == null) {
						local = new Integer(lvs.newLocal(Type.getType(field
								.getDesc())));
						Label l0 = new Label();
						visitLabel(l0);
						visitInsn(POP);
						mv.visitFieldInsn(GETSTATIC, field.getOwner(),
								DependencyConstants.KEY_FIELD_PREFIX
										+ field.getName(), Type
										.getDescriptor(Key.class));
						mv.visitMethodInsn(INVOKESTATIC, Type
								.getInternalName(DependencyLibrary.class),
								"locate",
								"(Lsalve/dependency/Key;)Ljava/lang/Object;");
						Label l1 = new Label();
						mv.visitLabel(l1);
						mv.visitTypeInsn(CHECKCAST, Type.getType(
								field.getDesc()).getInternalName());
						mv.visitVarInsn(ASTORE, local.intValue());
						Label l2 = new Label();
						mv.visitLabel(l2);
						mv.visitVarInsn(ALOAD, local.intValue());
						fieldToLocal.put(field, local);
					} else {
						mv.visitInsn(POP);
						mv.visitVarInsn(ALOAD, local.intValue());
					}
				} else {
					if (opcode == PUTFIELD) {
						super.visitFieldInsn(opcode, owner, name, desc);
						return;
					}
					if (local == null) {
						// first time access to this var, insert locator call
						Label l0 = new Label();
						mv.visitLabel(l0);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKESPECIAL, field.getOwner(),
								DependencyConstants.LOCATOR_METHOD_PREFIX
										+ field.getName(), "()V");
						fieldToLocal.put(field, new Integer(-1));
						mv.visitFieldInsn(opcode, owner, name, desc);
						return;
					} else {
						mv.visitFieldInsn(opcode, owner, name, desc);
						return;
					}

				}
			} else {
				mv.visitFieldInsn(opcode, owner, name, desc);
				return;
			}
		}

	}
}