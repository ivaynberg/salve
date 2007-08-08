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
package salve.depend;

import salve.AbstractInstrumentor;
import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationException;
import salve.InstrumentorMonitor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.depend.impl.ClassAnalyzer;
import salve.depend.impl.ClassInstrumentor;

public class DependencyInstrumentor extends AbstractInstrumentor {

	@Override protected byte[] internalInstrument(String className, BytecodeLoader loader, InstrumentorMonitor monitor)
			throws InstrumentationException {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		if (className == null) {
			throw new IllegalArgumentException("Argument `className` cannot be null");
		}

		className = className.trim();

		if (className.length() == 0) {
			throw new IllegalArgumentException("Argument `className` cannot be an empty");
		}

		try {
			byte[] bytecode = loader.loadBytecode(className);
			if (bytecode == null) {
				throw new CannotLoadBytecodeException(className);
			}

			ClassAnalyzer analyzer = new ClassAnalyzer(loader);
			ClassReader reader = new ClassReader(bytecode);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassInstrumentor inst = new ClassInstrumentor(writer, analyzer, monitor);
			reader.accept(inst, 0);

			return writer.toByteArray();
		} catch (Exception e) {
			// TODO message
			throw new InstrumentationException(e);
		}
	}
}
