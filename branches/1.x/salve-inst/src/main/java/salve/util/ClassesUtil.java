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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for dealing with classes
 * 
 * @author ivaynberg
 * 
 */
public class ClassesUtil {
	private static final String CLASS_LOADER_REFLECT_CLASS_NAME = "java.lang.ClassLoader";
	private static final String DEFINE_CLASS_METHOD_NAME = "defineClass";
	private static final Class<?>[] DEFINE_CLASS_METHOD_PARAMS = new Class[] { String.class, byte[].class, int.class,
			int.class };

	private ClassesUtil() {

	}

	/**
	 * Checks class name argument
	 * 
	 * @param className
	 *            class name
	 */
	public static void checkClassNameArg(String className) {
		if (className == null) {
			throw new IllegalArgumentException("Argument `className` cannot be null");
		}
		if (className.trim().length() != className.length()) {
			throw new IllegalArgumentException("Argument `className` cannot contain white space");
		}
		if (className.length() == 0) {
			throw new IllegalArgumentException("Argument `className` cannot be empty");
		}
	}

	/**
	 * Loads bytecode into a {@link Class} object
	 * 
	 * @param className
	 *            binary class name
	 * @param bytecode
	 *            bytecode
	 * @return created {@link Class} object
	 */
	public static Class<?> loadClass(final String className, byte[] bytecode) {
		checkClassNameArg(className);
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}

		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> klass = loader.loadClass(CLASS_LOADER_REFLECT_CLASS_NAME);
			Method method = klass.getDeclaredMethod(DEFINE_CLASS_METHOD_NAME, DEFINE_CLASS_METHOD_PARAMS);

			method.setAccessible(true);
			try {
				Object[] args = new Object[] { className.replace("/", "."), bytecode, 0, bytecode.length };
				Class<?> clazz = (Class<?>) method.invoke(loader, args);
				return clazz;
			} finally {
				method.setAccessible(false);
			}

		} catch (InvocationTargetException e) {
			// TODO exception
			throw new RuntimeException(e);
		} catch (Exception e) {
			// TODO exception
			throw new RuntimeException(e);
		}
	}

}
