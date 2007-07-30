/**
 * 
 */
package salve.dependency.impl;

import java.util.HashMap;
import java.util.Map;

import salve.dependency.DependencyLibrary;
import salve.dependency.IllegalFieldWriteException;
import salve.dependency.InjectionStrategy;
import salve.dependency.Key;
import salve.dependency.KeyImpl;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.FieldVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodAdapter;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Opcodes;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.LocalVariablesSorter;
import salve.org.objectweb.asm.commons.StaticInitMerger;

public class DependencyInstrumentorAdapter extends StaticInitMerger implements Opcodes {
	private final DependencyAnalyzer analyzer;
	private String owner = null;

	public DependencyInstrumentorAdapter(ClassVisitor cv, DependencyAnalyzer analyzer) {
		super("_salve$clinit", cv);
		this.analyzer = analyzer;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		owner = name;
		cv.visit(version, access, name, signature, superName, interfaces);
		generateKeyInitializerMethod();
		generateFieldInitiazerMethods();
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
			final Object value) {
		final DependencyField field = analyzer.locateField(owner, name);
		if (field != null) {
			FieldVisitor fv = null;
			if (field.getStrategy().equals(InjectionStrategy.REMOVE_FIELD)) {
				/*
				 * generate key field. we do this inplace here because it gives
				 * easier access to annotations on the removed field which we
				 * have to copy to key field
				 */
				final int a = ACC_PUBLIC + ACC_STATIC + ACC_FINAL;
				final String n = DependencyConstants.KEY_FIELD_PREFIX + name;
				final String d = Type.getDescriptor(Key.class);
				fv = cv.visitField(a, n, d, null, null);
			} else {
				fv = cv.visitField(access, name, desc, signature, value);
			}
			return new DependencyAnnotRemover(fv);
		} else {
			return cv.visitField(access, name, desc, signature, value);
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		boolean instrument = true;

		if ((access & ACC_STATIC) != 0) {
			instrument = false;
		} else if (name.startsWith(DependencyConstants.LOCATOR_METHOD_PREFIX)) {
			instrument = false;
		}

		if (instrument) {
			MethodInstrumentor inst = new MethodInstrumentor(access, desc, mv);
			inst.lvs = new LocalVariablesSorter(access, desc, inst);
			return inst.lvs;
		} else {
			return mv;
		}
	}

	private void generateFieldInitializerMethod(DependencyField field) {
		Type fieldOwnerType = Type.getObjectType(field.getOwner());
		Type fieldType = Type.getType(field.getDesc());

		MethodVisitor mv = cv.visitMethod(ACC_PRIVATE, DependencyConstants.LOCATOR_METHOD_PREFIX + field.getName(),
				"()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, fieldOwnerType.getInternalName(), field.getName(), field.getDesc());
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
		mv.visitMethodInsn(INVOKESPECIAL, "salve/dependency/KeyImpl", "<init>",
				"(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V");
		mv.visitVarInsn(ASTORE, 1);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESTATIC, "salve/dependency/DependencyLibrary", "locate",
				"(Lsalve/dependency/Key;)Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, fieldType.getInternalName());
		mv.visitFieldInsn(PUTFIELD, fieldOwnerType.getInternalName(), field.getName(), field.getDesc());
		mv.visitLabel(l1);
		mv.visitInsn(RETURN);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLocalVariable("this", fieldOwnerType.getDescriptor(), null, l0, l6, 0);
		mv.visitLocalVariable("key", "Lsalve/dependency/Key;", null, l5, l1, 1);
		mv.visitMaxs(5, 2);
		mv.visitEnd();
	}

	private void generateFieldInitiazerMethods() {
		for (DependencyField field : analyzer.locateFields(owner)) {
			if (InjectionStrategy.INJECT_FIELD == field.getStrategy()) {
				generateFieldInitializerMethod(field);
			}
		}
	}

	private void generateKeyInitializerMethod() {
		MethodVisitor mv = null;
		mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitCode();

		final String keyImplName = Type.getType(KeyImpl.class).getInternalName();

		for (DependencyField field : analyzer.locateFields(owner)) {
			if (InjectionStrategy.INJECT_FIELD.equals(field.getStrategy())) {
				continue;
			}
			final String fieldName = DependencyConstants.KEY_FIELD_PREFIX + field.getName();

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
			mv.visitMethodInsn(INVOKESPECIAL, keyImplName, "<init>",
					"(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V");
			mv.visitFieldInsn(PUTSTATIC, owner, fieldName, "Lsalve/dependency/Key;");
		}

		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private class MethodInstrumentor extends MethodAdapter implements Opcodes {
		public LocalVariablesSorter lvs;

		private Map<DependencyField, Integer> fieldToLocal;

		public MethodInstrumentor(int acc, String desc, MethodVisitor mv) {
			super(mv);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {

			if (opcode == GETSTATIC || opcode == PUTSTATIC) {
				super.visitFieldInsn(opcode, owner, name, desc);
				return;
			}

			DependencyField field = analyzer.locateField(owner, name);
			if (field != null) {

				if (opcode == PUTFIELD) {
					// throw IllegalFieldWriteException
					Label l0 = new Label();
					visitLabel(l0);
					visitTypeInsn(NEW, "salve/dependency/IllegalFieldWriteException");
					visitInsn(DUP);
					visitLdcInsn(field.getOwner().replace("/", "."));
					visitLdcInsn(field.getName());
					visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalFieldWriteException.class), "<init>",
							"(Ljava/lang/String;Ljava/lang/String;)V");
					visitInsn(ATHROW);
					return;
				}

				if (fieldToLocal == null) {
					fieldToLocal = new HashMap<DependencyField, Integer>();
				}

				Integer local = fieldToLocal.get(field);

				if (field.getStrategy().equals(InjectionStrategy.REMOVE_FIELD)) {
					if (local == null) {
						local = new Integer(lvs.newLocal(Type.getType(field.getDesc())));
						Label l0 = new Label();
						visitLabel(l0);
						visitInsn(POP);
						mv.visitFieldInsn(GETSTATIC, field.getOwner(), DependencyConstants.KEY_FIELD_PREFIX
								+ field.getName(), Type.getDescriptor(Key.class));
						mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(DependencyLibrary.class), "locate",
								"(Lsalve/dependency/Key;)Ljava/lang/Object;");
						Label l1 = new Label();
						mv.visitLabel(l1);
						mv.visitTypeInsn(CHECKCAST, Type.getType(field.getDesc()).getInternalName());
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
						mv.visitMethodInsn(INVOKESPECIAL, field.getOwner(), DependencyConstants.LOCATOR_METHOD_PREFIX
								+ field.getName(), "()V");
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(opcode, owner, name, desc);

						fieldToLocal.put(field, new Integer(-1));
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