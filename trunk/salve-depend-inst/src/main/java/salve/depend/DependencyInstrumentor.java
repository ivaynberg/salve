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
import salve.asmlib.Opcodes;
import salve.util.BytecodeLoadingClassWriter;
import salve.util.asm.AsmUtil;

/**
 * Instrumentor that instruments {@link Dependency} fields.
 * 
 * @see Dependency
 * @see DependencyLibrary
 * 
 * @author ivaynberg
 * 
 */
public class DependencyInstrumentor extends AbstractInstrumentor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected byte[] internalInstrument(String className, final BytecodeLoader loader, InstrumentorMonitor monitor)
			throws InstrumentationException {
		byte[] bytecode = loader.loadBytecode(className);
		if (bytecode == null) {
			throw new CannotLoadBytecodeException(className);
		}

		ClassReader reader = new ClassReader(bytecode);
		byte[] instrumented = bytecode;

		int access = reader.getAccess();
		if (AsmUtil.isSet(access, Opcodes.ACC_INTERFACE)) {
			// skip interfaces
		} else if (AsmUtil.isSet(access, Opcodes.ACC_ANNOTATION)) {
			// skip annotations
		} else if (AsmUtil.isSet(access, Opcodes.ACC_ENUM)) {
			// skip enums
		} else {
			ClassAnalyzer analyzer = new ClassAnalyzer(loader, className);
			if (analyzer.shouldInstrument()) {
				ClassWriter writer = new BytecodeLoadingClassWriter(ClassWriter.COMPUTE_FRAMES, loader);
				ClassInstrumentor inst = new ClassInstrumentor(writer, analyzer, monitor);
				reader.accept(inst, 0);
				instrumented = writer.toByteArray();
			}
		}
		return instrumented;
	}
}
