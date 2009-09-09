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

import salve.Bytecode;
import salve.BytecodeLoader;

/**
 * Simple {@link BytecodeLoader} that loads bytecode from the specified {@code
 * byte[]} array
 * 
 * @author ivaynberg
 * 
 */
public class MemoryLoader implements BytecodeLoader {
	private final String className;
	private final byte[] bytecode;

	/**
	 * Constructor
	 * 
	 * @param className
	 *            binary class name
	 * @param bytecode
	 *            bytecode
	 */
	public MemoryLoader(String className, byte[] bytecode) {
		super();
		if (className == null) {
			throw new IllegalArgumentException("Argument `className` cannot be null");
		}
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}
		this.bytecode = bytecode;
		this.className = className;
	}

	/**
	 * {@inheritDoc}
	 */
	public Bytecode loadBytecode(String className) {
		if (this.className.equals(className)) {
			return new Bytecode(className, bytecode, this);
		}
		return null;
	}

}
