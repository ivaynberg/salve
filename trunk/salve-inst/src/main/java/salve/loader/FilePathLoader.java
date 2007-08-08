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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FilePathLoader extends AbstractUrlLoader {
	private final File path;

	public FilePathLoader(File path) {
		if (path == null) {
			throw new IllegalArgumentException("Argument `path` cannot be null");
		}
		this.path = path;
	}

	@Override protected URL getBytecodeUrl(String className) {
		final String pathName = path.getAbsolutePath();
		final String fileName = className + ".class";
		if (pathName.endsWith(".jar") || pathName.endsWith(".zip")) {
			return findResourceInJar(path, fileName);
		} else {
			return findResourceInDir(path, fileName);
		}
	}

	private static URL findResourceInDir(File path, String name) {
		File file = new File(path.getAbsolutePath() + File.separator + name);
		if (file.exists()) {
			try {
				return file.getCanonicalFile().toURL();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}

		return null;
	}

	private static URL findResourceInJar(File path, String name) {
		String jarUrl;
		try {
			JarFile jar = new JarFile(path);
			JarEntry je = jar.getJarEntry(name);
			if (je != null) {
				jarUrl = path.getCanonicalFile().toURL().toString();
				return new URL("jar:" + jarUrl + "!/" + name);
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}

		return null;
	}
}
