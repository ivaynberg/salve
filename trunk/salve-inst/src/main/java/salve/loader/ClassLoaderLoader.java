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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import salve.BytecodeLoader;

public class ClassLoaderLoader implements BytecodeLoader {
	private final ClassLoader loader;

	public ClassLoaderLoader(ClassLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	public byte[] loadBytecode(String className) {
		InputStream is = loader.getResourceAsStream(className + ".class");
		if (is == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int read = 0;
		try {
			while ((read = is.read(buff)) > 0) {
				baos.write(buff, 0, read);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO exception: nicer message
			throw new RuntimeException("Could not read bytecode", e);
		}
	}
}
