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
package salve.depend.impl;

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

public class ClassAnalyzer implements Opcodes, Constants {

	private final BytecodeLoader loader;

	private final Set<String> owners = new HashSet<String>();
	private final Map<String, DependencyField> fields = new HashMap<String, DependencyField>();
	private final Map<String, List<DependencyField>> methodToFields = new HashMap<String, List<DependencyField>>();

	public ClassAnalyzer(BytecodeLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	public Collection<DependencyField> getDependenciesInClass(String owner) {
		// TODO check args
		processClass(owner);
		List<DependencyField> matches = new ArrayList<DependencyField>();
		for (DependencyField field : fields.values()) {
			if (field.getOwner().equals(owner)) {
				matches.add(field);
			}
		}
		return matches;
	}

	public Collection<DependencyField> getDependenciesInMethod(String name, String desc) {
		return methodToFields.get(name + desc);
	}

	public DependencyField getDependency(String owner, String name) {
		// TODO check args
		processClass(owner);
		return fields.get(fieldKey(owner, name));
	}

	/**
	 * @param owner
	 */
	private void processClass(String owner) {
		if (!owners.contains(owner)) {
			owners.add(owner);

			byte[] bytecode = loader.loadBytecode(owner);
			if (bytecode == null) {
				throw new CannotLoadBytecodeException(owner);
			}
			ClassReader reader = new ClassReader(bytecode);
			reader.accept(new Analyzer(), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);
		}
	}

	private class Analyzer extends ClassVisitorAdapter {
		private String owner;

		@Override public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			owner = name;
		}

		@Override public FieldVisitor visitField(final int fieldAccess, final String fieldName, final String fieldDesc,
				String signature, Object value) {
			return new FieldVisitorAdapter() {

				@Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					if (visible) {
						return visitFieldAnnotation(fieldAccess, fieldName, fieldDesc, desc);
					}
					return null;
				}
			};
		}

		@Override public MethodVisitor visitMethod(final int access, final String name, final String desc,
				final String signature, final String[] exceptions) {

			// do not ignore static methods becuase jvm creates synthetic static
			// methods that access fields
			if ((access & ACC_ABSTRACT) != 0 || name.startsWith(FIELDINIT_METHOD_PREFIX)) {
				return null;
			}

			final String method = name + desc;
			return new MethodVisitorAdapter() {
				@Override public void visitFieldInsn(int opcode, String owner, String name, String desc) {
					DependencyField field = getDependency(owner, name);
					if (field != null) {
						List<DependencyField> fields = methodToFields.get(method);
						if (fields == null) {
							fields = new ArrayList<DependencyField>(4);
							fields.add(field);
							methodToFields.put(method, fields);
						} else if (!fields.contains(field)) {
							fields.add(field);
						}
					}
				}
			};
		}

		private AnnotationVisitor visitFieldAnnotation(int fieldAcess, String fieldName, String fieldDesc,
				String annotDesc) {
			if (Constants.DEP_DESC.equals(annotDesc)) {
				final DependencyField field = new DependencyField(owner, fieldName, fieldDesc);

				fields.put(fieldKey(owner, fieldName), field);
				return new AnnotationVisitorAdapter() {

					@Override public void visitEnum(String name, String desc, String value) {

						if ("strategy".equals(name)) {
							field.setStrategy(value);
						}
					}

				};
			}
			return null;
		}
	}

	private static String fieldKey(String owner, String name) {
		return owner + "." + name;
	}
}
