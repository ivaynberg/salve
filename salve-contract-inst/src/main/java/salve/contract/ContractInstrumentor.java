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

import salve.AbstractInstrumentor;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.InstrumentorMonitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassVisitor;
import salve.asmlib.ClassWriter;
import salve.asmlib.MethodVisitor;
import salve.contract.impl.NotEmptyInstrumentor;
import salve.contract.impl.NotNullInstrumentor;
import salve.contract.impl.NumericalInstrumentor;
import salve.contract.pe.PeValidatorClassVisitor;
import salve.util.BytecodeLoadingClassWriter;

public class ContractInstrumentor extends AbstractInstrumentor {

	public static class ConditionalChecksInstrumentor extends ClassAdapter {
		private String owner;
		private final InstrumentorMonitor monitor;

		public ConditionalChecksInstrumentor(ClassVisitor cv, InstrumentorMonitor monitor) {
			super(cv);
			this.monitor = monitor;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			owner = name;
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
			mv = new NotNullInstrumentor(mv, monitor, owner, access, name, desc);
			mv = new NotEmptyInstrumentor(mv, monitor, owner, access, name, desc);
			mv = new NumericalInstrumentor(mv, monitor, owner, access, name, desc);
			return mv;
		}
	}

	@Override
	protected byte[] internalInstrument(String className, InstrumentationContext ctx) throws InstrumentationException {

		byte[] bytecode = ctx.getLoader().loadBytecode(className);
		if (bytecode == null) {
			throw new CannotLoadBytecodeException(className);
		}

		ClassReader reader = new ClassReader(bytecode);
		ContractAnalyzer analyzer = new ContractAnalyzer();
		reader.accept(new PeValidatorClassVisitor(ctx, analyzer), ClassReader.SKIP_FRAMES);

		if (analyzer.shouldInstrument()) {
			ClassWriter writer = new BytecodeLoadingClassWriter(ClassWriter.COMPUTE_FRAMES, ctx.getLoader());
			reader.accept(new ConditionalChecksInstrumentor(writer, ctx.getMonitor()), 0);
			bytecode = writer.toByteArray();
		}
		return bytecode;
	}

}
