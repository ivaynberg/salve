/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package salve.depend.spring.txn;

import salve.AbstractInstrumentor;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.util.BytecodeLoadingClassWriter;

public class OldTransactionalInstrumentor extends AbstractInstrumentor {

	protected byte[] internalInstrument(String className,
			InstrumentationContext ctx) throws InstrumentationException {

		byte[] bytecode = ctx.getLoader().loadBytecode(className);

		ClassReader reader = new ClassReader(bytecode);
		ClassWriter writer = new BytecodeLoadingClassWriter(
				ClassWriter.COMPUTE_FRAMES, ctx.getLoader());

		ClassAnalyzerImpl analyzer = new ClassAnalyzerImpl();
		reader.accept(analyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
				| ClassReader.SKIP_FRAMES);

		if (analyzer.shouldInstrument()) {
			ClassInstrumentor inst = new ClassInstrumentor(analyzer, writer,
					ctx.getMonitor());
			reader.accept(inst, 0);
			bytecode = writer.toByteArray();
		}

		return bytecode;
	}

}
