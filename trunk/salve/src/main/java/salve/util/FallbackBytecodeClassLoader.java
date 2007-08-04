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
package salve.util;

import salve.BytecodeLoader;

public class FallbackBytecodeClassLoader extends ClassLoader {
	private final BytecodeLoader bl;
	private final ClassLoader classLoader;

	public FallbackBytecodeClassLoader(ClassLoader classLoader, BytecodeLoader bytecodeLoader) {
		if (classLoader == null) {
			throw new IllegalArgumentException("Argument `delegate` cannot be null");
		}
		if (bytecodeLoader == null) {
			throw new IllegalArgumentException("Argument `bytecodeLoader` cannot be null");
		}
		this.bl = bytecodeLoader;
		this.classLoader = classLoader;
	}

	@Override public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return classLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
			return super.loadClass(name);
		}
	}

	@Override protected Class<?> findClass(String name) throws ClassNotFoundException {
		final String className = name.replace(".", "/");
		byte[] bytecode = bl.loadBytecode(className);
		if (bytecode == null) {
			throw new ClassNotFoundException(name);
		}
		return defineClass(name, bytecode, 0, bytecode.length);
	}

}
