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
import salve.Bytecode;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationContext;
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

	// private static class TracingBytecodeLoader implements BytecodeLoader {
	// private final BytecodeLoader delegate;
	//
	// public TracingBytecodeLoader(BytecodeLoader delegate) {
	// this.delegate = delegate;
	// }
	//
	// public byte[] loadBytecode(String className) {
	// if (!className.startsWith("biggie")) {
	// trace("loading bytecode: " + className);
	// Exception e = new Exception();
	// for (StackTraceElement ste : e.getStackTrace()) {
	// trace("  " + ste.toString());
	// }
	// }
	// return delegate.loadBytecode(className);
	// }
	//
	// }

	private static void trace(String str) {
		// try {
		// FileOutputStream fos = new FileOutputStream("c:/salve.txt", true);
		// PrintWriter out = new PrintWriter(fos);
		// out.println(str);
		// out.flush();
		// out.close();
		// } catch (FileNotFoundException e) {
		// }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected byte[] internalInstrument(String className, InstrumentationContext ctx) throws Exception {
		Bytecode bytecode = ctx.getLoader().loadBytecode(className);
		if (bytecode == null) {
			throw new CannotLoadBytecodeException(className);
		}

		byte[] bytes = bytecode.getBytes();
		ClassReader reader = new ClassReader(bytes);
		byte[] instrumented = bytes;

		int access = reader.getAccess();
		if (AsmUtil.isSet(access, Opcodes.ACC_INTERFACE)) {
			// skip interfaces
		} else if (AsmUtil.isSet(access, Opcodes.ACC_ANNOTATION)) {
			// skip annotations
		} else if (AsmUtil.isSet(access, Opcodes.ACC_ENUM)) {
			// skip enums

		} else {

			ClassAnalyzer analyzer = new ClassAnalyzer(className, ctx);
			if (analyzer.shouldInstrument() && !analyzer.isInstrumented(className)) {
				ClassWriter writer = new BytecodeLoadingClassWriter(ClassWriter.COMPUTE_FRAMES, ctx.getLoader());
				ClassInstrumentor inst = new ClassInstrumentor(writer, analyzer, ctx.getMonitor());
				reader.accept(inst, ClassReader.EXPAND_FRAMES);
				instrumented = writer.toByteArray();
			}

		}
		return instrumented;
	}

}
