/**
 * 
 */
package salve.depend.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.FieldVisitor;
import salve.asmlib.Label;
import salve.asmlib.LocalVariablesSorter;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.StaticInitMerger;
import salve.asmlib.Type;

public class ClassInstrumentor extends ClassAdapter implements Opcodes, Constants {
	private final ClassAnalyzer analyzer;
	private String owner = null;

	public ClassInstrumentor(ClassVisitor cv, ClassAnalyzer analyzer) {
		super(new StaticInitMerger(DEPNS + "_clinit", cv));
		this.analyzer = analyzer;
	}

	@Override public void visit(int version, int access, String name, String signature, String superName,
			String[] interfaces) {
		owner = name;
		cv.visit(version, access, name, signature, superName, interfaces);
		generateFieldInitiazerMethods();
	}

	@Override public void visitEnd() {
		addClinit();
		super.visitEnd();
	}

	@Override public FieldVisitor visitField(final int access, final String name, final String desc,
			final String signature, final Object value) {
		final DependencyField field = analyzer.getDependency(owner, name);
		if (field != null) {
			FieldVisitor fv = null;
			if (field.getStrategy().equals(STRAT_REMOVE)) {
				/*
				 * generate key field. we do this inplace here because it gives
				 * easier access to annotations on the removed field which we
				 * have to copy to key field
				 */
				final int a = ACC_PUBLIC + ACC_STATIC + ACC_FINAL;
				final String n = KEY_FIELD_PREFIX + name;
				final String d = KEY_DESC;
				fv = cv.visitField(a, n, d, null, null);
			} else {
				fv = cv.visitField(access, name, desc, signature, value);
			}
			return new DependencyAnnotRemover(fv);
		} else {
			return cv.visitField(access, name, desc, signature, value);
		}
	}

	@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature,
			String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

		boolean instrument = analyzer.getDependenciesInMethod(name, desc) != null;

		if (instrument) {
			MethodInstrumentor inst = new MethodInstrumentor(access, name, desc, mv);
			inst.lvs = new LocalVariablesSorter(access, desc, inst);
			return inst.lvs;
		} else {
			return mv;
		}
	}

	private void addClinit() {
		boolean hasRemoveFieldDependencies = false;
		for (DependencyField field : analyzer.getDependenciesInClass(owner)) {
			if (STRAT_REMOVE.equals(field.getStrategy())) {
				hasRemoveFieldDependencies = true;
				break;
			}
		}
		if (!hasRemoveFieldDependencies) {
			return;
		}

		MethodVisitor clinit = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
		clinit.visitCode();

		for (DependencyField field : analyzer.getDependenciesInClass(owner)) {
			if (STRAT_INJECT.equals(field.getStrategy())) {
				continue;
			}

			final String fieldName = KEY_FIELD_PREFIX + field.getName();

			clinit.visitLabel(new Label());
			clinit.visitTypeInsn(NEW, KEYIMPL_NAME);
			clinit.visitInsn(DUP);
			clinit.visitLabel(new Label());
			clinit.visitLdcInsn(field.getType());
			clinit.visitLdcInsn(Type.getObjectType(owner));
			clinit.visitLabel(new Label());
			clinit.visitLdcInsn(fieldName);
			clinit.visitLabel(new Label());
			clinit.visitMethodInsn(INVOKESPECIAL, KEYIMPL_NAME, "<init>", KEYIMPL_INIT_DESC);
			clinit.visitFieldInsn(PUTSTATIC, owner, fieldName, KEY_DESC);
		}

		clinit.visitInsn(RETURN);
		clinit.visitMaxs(0, 0);
		clinit.visitEnd();
	}

	/**
	 * Generates a method used to initialize a dependency field...
	 * 
	 * <pre>
	 * private void _salinit$dao() {
	 * 	if (dao == null) {
	 * 		Key key = new KeyImpl(Dao.class, getClass(), &quot;dao&quot;);
	 * 		dao = DependencyLibrary.locate(key);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param field
	 */
	private void generateFieldInitializerMethod(DependencyField field) {
		Type fieldOwnerType = Type.getObjectType(field.getOwner());

		MethodVisitor mv = cv.visitMethod(ACC_PRIVATE, FIELDINIT_METHOD_PREFIX + field.getName(), "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, fieldOwnerType.getInternalName(), field.getName(), field.getDesc());
		Label l1 = new Label();
		mv.visitJumpInsn(IFNONNULL, l1);

		mv.visitTypeInsn(NEW, KEYIMPL_NAME);
		mv.visitInsn(DUP);
		mv.visitLdcInsn(field.getType());

		mv.visitLdcInsn(Type.getType(fieldOwnerType.getDescriptor()));
		mv.visitLdcInsn(field.getName());

		mv.visitMethodInsn(INVOKESPECIAL, KEYIMPL_NAME, "<init>", KEYIMPL_INIT_DESC);
		mv.visitVarInsn(ASTORE, 1);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESTATIC, DEPLIB_NAME, DEPLIB_LOCATE_METHOD, DEPLIB_LOCATE_METHOD_DESC);
		mv.visitTypeInsn(CHECKCAST, field.getType().getInternalName());
		mv.visitFieldInsn(PUTFIELD, fieldOwnerType.getInternalName(), field.getName(), field.getDesc());
		mv.visitLabel(l1);
		mv.visitInsn(RETURN);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLocalVariable("this", fieldOwnerType.getDescriptor(), null, l0, l6, 0);
		mv.visitLocalVariable("key", KEY_DESC, null, l5, l1, 1);
		mv.visitMaxs(5, 2);
		mv.visitEnd();
	}

	private void generateFieldInitiazerMethods() {
		for (DependencyField field : analyzer.getDependenciesInClass(owner)) {
			if (STRAT_INJECT.equals(field.getStrategy())) {
				generateFieldInitializerMethod(field);
			}
		}
	}

	/**
	 * Generates bytecode to lazy-init a local with a dependency
	 * 
	 * <pre>
	 * 	if (local == null) {
	 * 		Key key = new KeyImpl(...);
	 * 		local = DependencyLibrary.locate(key);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param field
	 * @param local
	 */
	private void generateLoadDependencyIntoLocalBytecode(MethodVisitor mv, DependencyField field, int local) {

		mv.visitVarInsn(ALOAD, local);
		Label initialized = new Label();
		mv.visitJumpInsn(IFNONNULL, initialized);

		mv.visitFieldInsn(GETSTATIC, field.getOwner(), KEY_FIELD_PREFIX + field.getName(), KEY_DESC);

		mv.visitMethodInsn(INVOKESTATIC, DEPLIB_NAME, DEPLIB_LOCATE_METHOD, DEPLIB_LOCATE_METHOD_DESC);

		mv.visitTypeInsn(CHECKCAST, field.getType().getInternalName());
		mv.visitVarInsn(ASTORE, local);

		mv.visitLabel(initialized);
		mv.visitVarInsn(ALOAD, local);
	}

	/**
	 * @param field
	 */
	private void generateThrowIllegalFieldWriteExceptionBytecode(MethodVisitor mv, DependencyField field) {
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitTypeInsn(NEW, IFWE_NAME);
		mv.visitInsn(DUP);
		mv.visitLdcInsn(field.getOwner().replace("/", "."));
		mv.visitLdcInsn(field.getName());
		mv.visitMethodInsn(INVOKESPECIAL, IFWE_NAME, "<init>", IFWE_INIT_DESC);
		mv.visitInsn(ATHROW);
	}

	private class MethodInstrumentor extends MethodAdapter implements Opcodes {
		private LocalVariablesSorter lvs;
		private Map<DependencyField, Integer> fieldToLocal;
		private final Collection<DependencyField> referencedFields;

		public MethodInstrumentor(int acc, String name, String desc, MethodVisitor mv) {
			super(mv);
			referencedFields = analyzer.getDependenciesInMethod(name, desc);
		}

		@Override public void visitCode() {
			super.visitCode();
			if (referencedFields != null) {
				for (DependencyField field : referencedFields) {
					if (STRAT_REMOVE.equals(field.getStrategy())) {
						final int local = lvs.newLocal(field.getType());
						setLocalForField(field, local);
						// init local to null
						mv.visitInsn(ACONST_NULL);
						mv.visitVarInsn(ASTORE, local);
					}
				}
			}
		}

		@Override public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {

			// Keep in mind this instruction is always preceded by ALOAD 0 ;this

			boolean instrument = false;
			DependencyField field = null;

			if (opcode == GETSTATIC || opcode == PUTSTATIC) {
				instrument = false;
			} else {
				field = analyzer.getDependency(owner, name);
				instrument = field != null;
			}

			if (!instrument) {
				mv.visitFieldInsn(opcode, owner, name, desc);
				return;
			}

			if (opcode == PUTFIELD) {
				mv.visitInsn(POP); // Pop off ALOAD 0 ;this
				generateThrowIllegalFieldWriteExceptionBytecode(mv, field);
				return;
			}

			if (STRAT_REMOVE.equals(field.getStrategy())) {
				final int local = getLocalForField(field);
				visitInsn(POP);// Pop off ALOAD 0 ;this
				generateLoadDependencyIntoLocalBytecode(mv, field, local);
			} else if (STRAT_INJECT.equals(field.getStrategy())) {
				// ALOAD 0 ;this is already on the stack
				mv.visitMethodInsn(INVOKESPECIAL, field.getOwner(), FIELDINIT_METHOD_PREFIX + field.getName(), "()V");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(opcode, owner, name, desc);
			}

		}

		private int getLocalForField(DependencyField field) {
			return fieldToLocal.get(field);
		}

		private void setLocalForField(DependencyField field, int local) {
			if (fieldToLocal == null) {
				fieldToLocal = new HashMap<DependencyField, Integer>();
			}
			fieldToLocal.put(field, local);

		}

	}

}