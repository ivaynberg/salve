/**
 * 
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.depend;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import salve.CodeMarker;
import salve.InstrumentationContext;
import salve.asmlib.AnnotationVisitor;
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

/**
 * INTERNAL
 * <p>
 * Instruments class to implement {@link Dependency} field resolution
 * </p>
 * 
 * @author ivaynberg
 * 
 */
class ClassInstrumentor extends ClassAdapter implements Opcodes, Constants {
	/**
	 * Method instrumentor
	 * 
	 * @author ivaynberg
	 */
	private class MethodInstrumentor extends MethodAdapter implements Opcodes {
		private LocalVariablesSorter lvs;
		private Map<DependencyField, Integer> fieldToLocal;
		private final Collection<DependencyField> referencedFields;
		private CodeMarker currentMarker;

		/**
		 * Constructor
		 * 
		 * @param acc
		 *            method access flags
		 * @param name
		 *            method name
		 * @param desc
		 *            method descriptor
		 * @param mv
		 *            method visitor
		 */
		public MethodInstrumentor(int acc, String name, String desc, MethodVisitor mv) {
			super(mv);
			referencedFields = analyzer.getDependenciesInMethod(owner, name, desc);
		}

		/**
		 * Looks up index of local variable that replaces access to depenedncy
		 * field
		 * 
		 * @param field
		 * @return index of local var
		 */
		private int getLocalForField(DependencyField field) {
			return fieldToLocal.get(field);
		}

		/**
		 * Sets index of local variable that will replace access to dependenc
		 * field
		 * 
		 * @param field
		 * @param local
		 */
		private void setLocalForField(DependencyField field, int local) {
			if (fieldToLocal == null) {
				fieldToLocal = new HashMap<DependencyField, Integer>();
			}
			fieldToLocal.put(field, local);

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitCode() {
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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {

			// Keep in mind this instruction is always preceded by ALOAD 0

			boolean instrument = false;
			DependencyField field = null;

			field = analyzer.getDependency(owner, name);
			instrument = field != null;

			if (!instrument) {
				mv.visitFieldInsn(opcode, owner, name, desc);
				return;
			}

			if (opcode == GETFIELD || opcode == PUTFIELD) {
				visitInsn(POP);// Pop off ALOAD 0 ;this
			}

			if (opcode == PUTFIELD || opcode == PUTSTATIC) {
				throwIllegalFieldWriteException(mv, field);
				return;
			}

			if (STRAT_REMOVE.equals(field.getStrategy())) {
				final int local = getLocalForField(field);
				loadDependencyIntoLocal(mv, field, local);
			} else if (STRAT_INJECT.equals(field.getStrategy())) {
				// ALOAD 0 ;this is already on the stack
				mv.visitVarInsn(ALOAD, 0);
				loadDependencyIntoField(mv, field);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(opcode, owner, name, desc);
			}

			ctx.getLogger().info(currentMarker, "Acess to field '%s' intercepted by @Dependency instrumentor", name);

		}

		@Override
		public void visitLineNumber(int line, Label start) {
			currentMarker = new CodeMarker(owner, line);
			super.visitLineNumber(line, start);
		}
	}

	private final ClassAnalyzer analyzer;
	private String owner = null;

	private final InstrumentationContext ctx;

	/**
	 * Constructor
	 * 
	 * @param cv
	 *            class visitor
	 * @param analyzer
	 *            analyzer
	 * @param monitor
	 *            instrumentor monitor
	 */
	public ClassInstrumentor(ClassVisitor cv, ClassAnalyzer analyzer, InstrumentationContext ctx) {
		super(new StaticInitMerger(DEPNS + "_clinit", cv));
		this.analyzer = analyzer;
		this.ctx = ctx;
	}

	/**
	 * Adds clinit method used to initialze any added static fields
	 */
	private void addClinit() {
		boolean instrument = !analyzer.getDependenciesInClass(owner).isEmpty();
		if (!instrument) {
			return;
		}

		final int acc = ACC_STATIC;
		final String name = "<clinit>";
		final String desc = "()V";
		MethodVisitor clinit = cv.visitMethod(acc, name, desc, null, null);
		ctx.getMonitor().methodModified(owner, acc, name, desc);
		clinit.visitCode();

		for (DependencyField field : analyzer.getDependenciesInClass(owner)) {
			final String keyFieldName = getKeyFieldName(field);
			final String fieldName = getDependencyFieldName(field);
			clinit.visitTypeInsn(NEW, KEYIMPL_NAME);
			clinit.visitInsn(DUP);
			clinit.visitLdcInsn(Type.getObjectType(owner));
			clinit.visitLdcInsn(fieldName);
			clinit.visitMethodInsn(INVOKESPECIAL, KEYIMPL_NAME, "<init>", KEYIMPL_INIT_DESC);
			clinit.visitFieldInsn(PUTSTATIC, owner, keyFieldName, KEY_DESC);
		}

		clinit.visitInsn(RETURN);
		clinit.visitMaxs(0, 0);
		clinit.visitEnd();
	}

	private String getDependencyFieldName(DependencyField field) {
		return STRAT_INJECT.equals(field.getStrategy()) ? field.getName() : REMOVED_FIELD_PREFIX + field.getName();
	}

	private String getKeyFieldName(DependencyField field) {
		return KEY_FIELD_PREFIX + field.getName();
	}

	/**
	 * Generates bytecode to lazy-init a field with a dependency
	 * 
	 * <pre>
	 * 	if (field == null) {
	 * 		Key key = STATIC_KEY_HOLDER_FIELD;
	 * 		field = DependencyLibrary.locate(key);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param field
	 * @param local
	 */
	private void loadDependencyIntoField(MethodVisitor mv, DependencyField field) {
		final Type fieldOwnerType = Type.getObjectType(field.getOwner());

		// mv.visitVarInsn(ALOAD, 0); already on the stack

		mv.visitFieldInsn(GETFIELD, fieldOwnerType.getInternalName(), field.getName(), field.getDesc());
		Label initialized = new Label();
		mv.visitJumpInsn(IFNONNULL, initialized);
		mv.visitVarInsn(ALOAD, 0); // needed for putfield
		loadDependencyOntoStack(mv, field);
		mv.visitFieldInsn(PUTFIELD, fieldOwnerType.getInternalName(), field.getName(), field.getDesc());
		mv.visitLabel(initialized);
	}

	/**
	 * Generates bytecode to lazy-init a local with a dependency
	 * 
	 * <pre>
	 * 	if (local == null) {
	 * 		Key key = STATIC_KEY_HOLDER_FIELD;
	 * 		local = DependencyLibrary.locate(key);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param field
	 * @param local
	 */
	private void loadDependencyIntoLocal(MethodVisitor mv, DependencyField field, int local) {
		mv.visitVarInsn(ALOAD, local);
		Label initialized = new Label();
		mv.visitJumpInsn(IFNONNULL, initialized);
		loadDependencyOntoStack(mv, field);
		mv.visitVarInsn(ASTORE, local);
		mv.visitLabel(initialized);
		mv.visitVarInsn(ALOAD, local);
	}

	/**
	 * Looks up dependency and pushes it onto the stack
	 * 
	 * @param mv
	 *            method visitor
	 * @param field
	 *            dependency field
	 */
	private void loadDependencyOntoStack(MethodVisitor mv, DependencyField field) {
		mv.visitFieldInsn(GETSTATIC, field.getOwner(), getKeyFieldName(field), KEY_DESC);
		mv.visitMethodInsn(INVOKESTATIC, DEPLIB_NAME, DEPLIB_LOCATE_METHOD, DEPLIB_LOCATE_METHOD_DESC);
		mv.visitTypeInsn(CHECKCAST, field.getType().getInternalName());
	}

	/**
	 * generates bytecode to throw an {@link IllegalFieldWriteException}
	 * exception
	 * 
	 * @param field
	 *            field for which the exception needs to be thrown
	 */
	private void throwIllegalFieldWriteException(MethodVisitor mv, DependencyField field) {
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitTypeInsn(NEW, IFWE_NAME);
		mv.visitInsn(DUP);
		mv.visitLdcInsn(field.getOwner().replace("/", "."));
		mv.visitLdcInsn(field.getName());
		mv.visitMethodInsn(INVOKESPECIAL, IFWE_NAME, "<init>", IFWE_INIT_DESC);
		mv.visitInsn(ATHROW);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		owner = name;
		cv.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitEnd() {
		cv.visitAnnotation(INSTRUMENTED_DESC, true);
		addClinit();
		super.visitEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
			final Object value) {

		final DependencyField field = analyzer.getDependency(owner, name);
		if (field != null) {
			{
				// generate salve.depend.Key field
				final int a = ACC_PUBLIC + ACC_STATIC + ACC_FINAL;
				final String n = KEY_FIELD_PREFIX + name;
				final String d = KEY_DESC;
				cv.visitField(a, n, d, null, null);
				ctx.getMonitor().fieldAdded(owner, a, n, d);
			}

			FieldVisitor fv = null;
			if (field.getStrategy().equals(STRAT_REMOVE)) {
				/*
				 * using remove-field strategy, move the field into static space
				 * and rename it. we do not remove the field completely because
				 * we still need access to java.lang.reflect.Field object for
				 * dependency resolution
				 */
				final int a = ACC_PUBLIC + ACC_STATIC + ACC_FINAL;
				final String n = getDependencyFieldName(field);

				ctx.getMonitor().fieldRemoved(owner, access, name, desc);
				ctx.getMonitor().fieldAdded(owner, a, n, desc);

				fv = cv.visitField(a, n, desc, signature, null);

				// since we are removing the field we have to add SalveFieldInfo
				// annotation so we do not lose field info for later bytecode
				// passes

				AnnotationVisitor annot = fv.visitAnnotation(FieldInfo.DESC, true);
				annot.visit(FieldInfo.Params.NAME, name);
				annot.visit(FieldInfo.Params.DESC, desc);
			} else {
				/*
				 * using inject-field strategy, make field transient so object
				 * can be serialized
				 */
				fv = cv.visitField(access + ACC_TRANSIENT, name, desc, signature, value);
			}

			return new DependencyAnnotRemover(fv);
		} else {
			return cv.visitField(access, name, desc, signature, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

		boolean instrument = analyzer.getDependenciesInMethod(owner, name, desc) != null;

		if (instrument) {
			ctx.getMonitor().methodModified(owner, access, name, desc);
			MethodInstrumentor inst = new MethodInstrumentor(access, name, desc, mv);
			inst.lvs = new LocalVariablesSorter(access, desc, inst);
			return inst.lvs;
		} else {
			return mv;
		}
	}

}