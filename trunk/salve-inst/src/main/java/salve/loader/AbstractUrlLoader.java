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
import java.net.URL;

import salve.BytecodeLoader;

public abstract class AbstractUrlLoader implements BytecodeLoader {

	public final byte[] loadBytecode(String className) {
		URL url = getBytecodeUrl(className);
		if (url == null) {
			return null;
		} else {
			try {
				final InputStream in = url.openStream();
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final byte[] buff = new byte[1024];
				while (true) {
					int read = in.read(buff, 0, buff.length);
					if (read <= 0) {
						break;
					}
					out.write(buff, 0, read);
				}
				return out.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException("Could not read bytecode from " + url.toString(), e);
			}
		}
	}

	protected abstract URL getBytecodeUrl(String className);

}
