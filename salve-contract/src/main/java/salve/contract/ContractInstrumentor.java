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
package salve.contract;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.contract.impl.NotEmptyInstrumentor;
import salve.contract.impl.NotNullInstrumentor;
import salve.contract.impl.OMIAnalyzer;
import salve.contract.impl.OMIInstrumentor;
import salve.org.objectweb.asm.ClassReader;
import salve.org.objectweb.asm.ClassWriter;

public class ContractInstrumentor implements Instrumentor {

	public byte[] instrument(String className, BytecodeLoader loader)
			throws InstrumentationException {
		// FIXME factor out these arg checks into an abstract instrumentor
		if (loader == null) {
			throw new IllegalArgumentException(
					"Argument `loader` cannot be null");
		}
		if (className == null) {
			throw new IllegalArgumentException(
					"Argument `className` cannot be null");
		}

		className = className.trim();

		if (className.length() == 0) {
			throw new IllegalArgumentException(
					"Argument `className` cannot be an empty");
		}

		try {
			byte[] bytecode = loader.loadBytecode(className);
			if (bytecode == null) {
				throw new CannotLoadBytecodeException(className);
			}

			{
				ClassReader reader = new ClassReader(bytecode);
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				reader.accept(new NotNullInstrumentor(writer), 0);
				bytecode = writer.toByteArray();
			}
			{
				ClassReader reader = new ClassReader(bytecode);
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				reader.accept(new NotEmptyInstrumentor(writer), 0);
				bytecode = writer.toByteArray();
			}

			{
				ClassReader reader = new ClassReader(bytecode);
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

				OMIAnalyzer analyzer = new OMIAnalyzer();
				analyzer.analyze(bytecode, loader);
				reader.accept(new OMIInstrumentor(writer, analyzer), 0);
				bytecode = writer.toByteArray();
			}
			return bytecode;

		} catch (Exception e) {
			// TODO message
			throw new InstrumentationException(e);
		}
	}

}
