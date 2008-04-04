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
package salve.eclipse.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;

public class FileBytecodeLoader implements BytecodeLoader {
	private final IFile file;

	public FileBytecodeLoader(IFile file) {
		if (file == null) {
			throw new IllegalArgumentException("Argument `file` cannot be null");
		}
		this.file = file;
	}

	public byte[] loadBytecode(String className) {
		if (!file.getFullPath().toString().endsWith(className + ".class")) {
			return null;
		}
		InputStream in = null;
		try {
			in = file.getContents();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			while (true) {
				int read = in.read(buff);
				if (read < 1) {
					break;
				}
				out.write(buff, 0, read);
			}
			return out.toByteArray();
		} catch (Exception e) {
			throw new CannotLoadBytecodeException(className, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(
							"Could not close input stream for file: "
									+ file.getFullPath().toString());
				}
			}
		}
	}
}
