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
package salve.loader;

import java.io.InputStream;

import salve.BytecodeLoader;
import salve.util.StreamsUtil;

/**
 * {@link BytecodeLoader} that loads bytecode using
 * {@link ClassLoader#getResource(String)}
 * 
 * @author ivaynberg
 */
public class ClassLoaderLoader implements BytecodeLoader {
	private static final String ERROR_MSG = "Error reading input stream for class {}, classloader {}";
	private final ClassLoader loader;

	/**
	 * Constructor
	 * 
	 * @param loader
	 *            class loader to use
	 */
	public ClassLoaderLoader(ClassLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] loadBytecode(String className) {
		InputStream in = loader.getResourceAsStream(className + ".class");
		if (in != null) {
			return StreamsUtil.drain(in, ERROR_MSG, className, loader);
		} else {
			return null;
		}
	}
}
