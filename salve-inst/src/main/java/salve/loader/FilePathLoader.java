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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import salve.Bytecode;
import salve.BytecodeLoader;
import salve.util.StreamsUtil;

/**
 * Bytecode loader that can load bytecode from specified file path. The file
 * path can point to a directory, a zip, or a jar
 * 
 * @author ivaynberg
 */
public class FilePathLoader implements BytecodeLoader {
	private static final String FILE_ERROR_MSG = "Could not read bytecode from {}";
	private final File base;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            file path
	 */
	public FilePathLoader(File path) {
		if (path == null) {
			throw new IllegalArgumentException("Argument `path` cannot be null");
		} else if (!path.exists()) {
			throw new IllegalArgumentException("File `path` does not exist: " + path.getAbsolutePath());
		}
		this.base = path;
	}

	protected JarEntry findJarEntry(File jar, String name) throws IOException {
		JarFile archive = new JarFile(jar);
		try {
			JarEntry entry = (JarEntry) archive.getEntry(name);
			return entry;
		} finally {
			if (archive != null) {
				archive.close();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Bytecode loadBytecode(String className) {
		final String fileName = className + ".class";
		final String pathName = base.getAbsolutePath();
		if (pathName.endsWith(".jar") || pathName.endsWith(".zip")) {
			return loadFromJar(className, base, fileName);
		} else {
			return loadFromFile(className, new File(base, fileName));
		}
	}

	protected Bytecode loadFromFile(String className, File file) {
		// do not check if file exists upfront as it is an expensive check,
		// instead handle thatvia FileNotFoundException

		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// throw new RuntimeException("Could not open file for reading
			// bytecode: " + file.getAbsolutePath(), e);
			return null;
		}

		try {
			final byte[] bytes = StreamsUtil.drain(fis, FILE_ERROR_MSG, file.getAbsolutePath());
			return new Bytecode(className, bytes, this);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof FileNotFoundException) {
				return null;
			}
			throw e;
		}

	}

	protected Bytecode loadFromJar(String className, File jar, String name) {
		// do not check if file exists upfront as it is an expensive check,
		// instead handle thatvia FileNotFoundException

		try {
			byte[] bytes = null;

			JarEntry entry = findJarEntry(jar, name);
			if (entry != null) {
				bytes = readJarEntry(jar, entry);
			}
			if (bytes == null) {
				return null;
			} else {
				return new Bytecode(className, bytes, this);
			}
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException("Could not read bytecode for " + name + "@jar: " + jar.getAbsolutePath(), e);
		}
	}

	protected byte[] readJarEntry(File jar, JarEntry entry) throws IOException {
		JarFile archive = new JarFile(jar);
		try {
			InputStream in = null;
			try {
				in = archive.getInputStream(entry);
				return StreamsUtil.drain(in);
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} finally {
			archive.close();
		}
	}
}
