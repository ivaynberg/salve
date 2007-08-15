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
 * Base class for {@link Instrumentor} implementations
 * 
 * @author ivaynberg
 * 
 */
public abstract class AbstractInstrumentor implements Instrumentor {

	/**
	 * {@inheritDoc}
	 */
	public byte[] instrument(String className, BytecodeLoader loader, InstrumentorMonitor monitor)
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
			return internalInstrument(className, loader, monitor);
		} catch (Exception e) {
			throw new InstrumentationException("Error instrumenting " + className + " with instrumentor "
					+ getClass().getName(), e);
		}
	}

	/**
	 * Internal forward of
	 * {@link #instrument(String, BytecodeLoader, InstrumentorMonitor)}
	 * 
	 * @param className
	 * @param loader
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	protected abstract byte[] internalInstrument(String className, BytecodeLoader loader, InstrumentorMonitor monitor)
			throws Exception;

}
