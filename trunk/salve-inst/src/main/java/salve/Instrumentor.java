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
package salve;

/**
 * Represents a bytecode instrumentor
 * 
 * @author ivaynberg
 */
public interface Instrumentor {
	/**
	 * Instruments bytecode
	 * 
	 * @param className
	 *            fully qualified class name (salve/asm/BytecodeLoader)
	 * @param loader
	 *            bytecodeLoader that can be used to load bytecode for specified
	 *            class and any referenced classes
	 * @return instrumented bytecode
	 * @throws Exception
	 *             if instrumentation fails
	 */
	byte[] instrument(String className, BytecodeLoader loader, InstrumentorMonitor monitor)
			throws InstrumentationException;
}
