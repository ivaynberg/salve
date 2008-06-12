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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassReader;
import salve.asmlib.FieldVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.util.asm.AnnotationVisitorAdapter;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.FieldVisitorAdapter;
import salve.util.asm.MethodVisitorAdapter;

/**
 * INTERNAL
 * <p>
 * Gathers information about class being instrumented so that
 * {@link ClassInstrumentor} has it available
 * </p>
 * 
 * @author ivaynberg
 */
class ClassAnalyzer implements Opcodes, Constants {

	/**
	 * Class visitor that performs the analysis
	 * 
	 * @author ivaynberg
	 * 
	 */
	private class Analyzer extends ClassVisitorAdapter {
		private String owner;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			owner = name;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public FieldVisitor visitField(final int fieldAccess, final String fieldName, final String fieldDesc,
				String signature, Object value) {
			return new FieldVisitorAdapter() {

				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					return visitFieldAnnotation(fieldAccess, fieldName, fieldDesc, desc);
				}
			};
		}

		/**
		 * called when a field within class being analyzed is visited
		 */
		private AnnotationVisitor visitFieldAnnotation(int fieldAcess, String fieldName, String fieldDesc,
				String annotDesc) {
			if (Constants.DEP_DESC.equals(annotDesc)) {
				final DependencyField field = new DependencyField(owner, fieldName, fieldDesc);

				fieldKeyToField.put(generateFieldKey(owner, fieldName), field);
				return new AnnotationVisitorAdapter() {

					@Override
					public void visitEnum(String name, String desc, String value) {

						if ("strategy".equals(name)) {
							field.setStrategy(value);
						}
					}

				};
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MethodVisitor visitMethod(final int access, final String name, final String desc,
				final String signature, final String[] exceptions) {

			// do not ignore static methods because jvm creates synthetic static
			// methods that access fields
			// XXX check if we can check for ACC_SYNTHETIC here
			if ((access & ACC_ABSTRACT) != 0 || name.startsWith(FIELDINIT_METHOD_PREFIX)) {
				return null;
			}

			final String methodKey = generateMethodKey(owner, name, desc);
			return new MethodVisitorAdapter() {
				@Override
				public void visitFieldInsn(int opcode, String owner, String name, String desc) {
					DependencyField field = getDependency(owner, name);
					if (field != null) {
						List<DependencyField> fields = methodKeyToFields.get(methodKey);
						if (fields == null) {
							fields = new ArrayList<DependencyField>(4);
							fields.add(field);
							methodKeyToFields.put(methodKey, fields);
						} else if (!fields.contains(field)) {
							fields.add(field);
						}
					}
				}
			};
		}
	}

	/**
	 * Generates a field key used to uniquely identify a field within classes
	 * 
	 * @param className
	 *            binary name of the class that contains the field
	 * @param fieldName
	 *            field name
	 * @return generated field key
	 */
	private static String generateFieldKey(String className, String fieldName) {
		return className + "." + fieldName;
	}

	/**
	 * Generates a method key to uniquely identify a method within classes
	 * 
	 * @param className
	 *            binary class name of the class that contains the method
	 * @param methodName
	 *            method name
	 * @param methodDescriptor
	 *            method descriptor
	 * @return generated method key
	 */
	private static String generateMethodKey(String className, String methodName, String methodDescriptor) {
		return className + "." + methodName + methodDescriptor;
	}

	private final BytecodeLoader loader;

	private final Set<String> scannedClasses = new HashSet<String>();

	private final Map<String, DependencyField> fieldKeyToField = new HashMap<String, DependencyField>();

	private final Map<String, List<DependencyField>> methodKeyToFields = new HashMap<String, List<DependencyField>>();

	/**
	 * Constructor
	 * 
	 * @param loader
	 *            bytecode loader
	 */
	public ClassAnalyzer(BytecodeLoader loader, String className) {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		this.loader = loader;
		analyzeClass(className);
	}

	/**
	 * Analyzes the specified class
	 * 
	 * @param owner
	 *            binary class name
	 */
	private void analyzeClass(String owner) {
		if (!scannedClasses.contains(owner)) {
			scannedClasses.add(owner);

			byte[] bytecode = loader.loadBytecode(owner);
			if (bytecode == null) {
				throw new CannotLoadBytecodeException(owner);
			}
			ClassReader reader = new ClassReader(bytecode);
			reader.accept(new Analyzer(), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);
		}
	}

	public boolean shouldInstrument() {
		return fieldKeyToField.size() > 0 || methodKeyToFields.size() > 0;
	}

	/**
	 * @param owner
	 *            binary class name
	 * @return a {@link DependencyField}s declared in the specified class
	 */
	public Collection<DependencyField> getDependenciesInClass(String owner) {
		// TODO check args
		analyzeClass(owner);
		List<DependencyField> matches = new ArrayList<DependencyField>();
		for (DependencyField field : fieldKeyToField.values()) {
			if (field.getOwner().equals(owner)) {
				matches.add(field);
			}
		}
		return matches;
	}

	/**
	 * @param owner
	 *            name of class that owns the method
	 * @param name
	 *            binary class name
	 * @param desc
	 *            method descriptor
	 * @return {@link DependencyField}s that are accessed within the specified
	 *         method
	 */
	public Collection<DependencyField> getDependenciesInMethod(String owner, String name, String desc) {
		return methodKeyToFields.get(generateMethodKey(owner, name, desc));
	}

	/**
	 * Looks up a dependency field in the specified class
	 * 
	 * @param owner
	 *            binary class name
	 * @param fieldName
	 *            field name
	 * @return {@link DependencyField} or null if the field is not a dependency
	 *         field
	 */
	public DependencyField getDependency(String owner, String fieldName) {
		// TODO check args
		analyzeClass(owner);
		return fieldKeyToField.get(generateFieldKey(owner, fieldName));
	}
}
