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
package salve.contract.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import salve.BytecodeLoader;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassReader;
import salve.asmlib.MethodVisitor;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.MethodVisitorAdapter;

public class OMIAnalyzer {
	private static final int QUICK_MODE = ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES;

	private static final String OBJECT = "java/lang/Object";

	private final ArrayList<String> omiMethods = new ArrayList<String>();

	public OMIAnalyzer analyze(byte[] bytecode, BytecodeLoader loader) {
		omiMethods.clear();

		Set<String> methods = new HashSet<String>();
		Set<String> visitedInterfaces = new HashSet<String>();

		ClassReader reader = new ClassReader(bytecode);
		if (OBJECT.equals(reader.getClassName())) {
			return this;
		}

		reader.accept(new MethodCollector(methods), QUICK_MODE);

		while (!OBJECT.equals(reader.getSuperName())) {
			for (String iface : reader.getInterfaces()) {
				visitInterface(iface, methods, visitedInterfaces, loader);
			}
			reader = new ClassReader(loader.loadBytecode(reader.getSuperName()));
			reader.accept(new OMIMethodIdentifier(methods), QUICK_MODE);
		}
		return this;
	}

	public boolean shouldInstrument(String name, String desc) {
		return omiMethods.contains(name + desc);
	}

	private void visitInterface(String iface, Set<String> methods, Set<String> visitedInterfaces, BytecodeLoader loader) {
		if (!visitedInterfaces.contains(iface)) {
			visitedInterfaces.add(iface);
			ClassReader reader = new ClassReader(loader.loadBytecode(iface));
			reader.accept(new OMIMethodIdentifier(methods), QUICK_MODE);
			for (String ifc : reader.getInterfaces()) {
				visitInterface(ifc, methods, visitedInterfaces, loader);
			}
		}
	}

	public static class MethodCollector extends ClassVisitorAdapter {
		private final Collection<String> methods;

		public MethodCollector(Collection<String> methods) {
			this.methods = methods;
		}

		@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature,
				String[] exceptions) {
			if (!"<init>".equals(name) && !"<clinit>".equals(name)) {
				methods.add(name + desc);
			}
			return null;
		}
	}

	public class OMIMethodIdentifier extends ClassVisitorAdapter implements Constants {
		private final Set<String> allowedMethods;

		public OMIMethodIdentifier(Set<String> allowedMethods) {
			this.allowedMethods = allowedMethods;
		}

		@Override public MethodVisitor visitMethod(int access, String name, String desc, String signature,
				String[] exceptions) {
			final String method = name + desc;
			if (allowedMethods.contains(method)) {
				return new MethodVisitorAdapter() {
					@Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
						if (OMI.getDescriptor().equals(desc)) {
							allowedMethods.remove(method);
							omiMethods.add(method);
						}
						return null;
					}
				};
			} else {
				return null;
			}
		}
	}
}
