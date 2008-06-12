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
 * Exception thrown when an instrumentor cannot load bytecode of a class
 * 
 * @author ivaynberg
 */
public class CannotLoadBytecodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param className
	 *            binary name of class for which bytecode could not be loaded
	 */
	public CannotLoadBytecodeException(String className) {
		super("Cannot load bytecode for class " + className);
	}

	/**
	 * Constructor
	 * 
	 * @param className
	 *            binary name of class for which bytecode could not be loaded
	 * @param e
	 *            cause of the exception
	 */
	public CannotLoadBytecodeException(String className, Throwable e) {
		super("Cannot load bytecode for class " + className, e);
	}
}
