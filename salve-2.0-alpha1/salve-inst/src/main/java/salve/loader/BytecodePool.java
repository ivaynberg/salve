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
import salve.Scope;
import salve.model.ProjectModel;
import salve.util.ClassesUtil;
import salve.util.LruCache;

/**
 * A caching {@link BytecodeLoader} implementation that contains various
 * bytecode utility methods.
 * 
 * @author ivaynberg
 * 
 */
public class BytecodePool extends CompoundLoader {

	private static final Bytecode NOT_FOUND = new Bytecode();

	private final LruCache<String, Bytecode> cache = new LruCache<String, Bytecode>(5000);

	protected final Scope scope;

	// FIXME remove
	protected final ProjectModel model;

	/**
	 * Constructor
	 * 
	 * @param scope
	 *            instrumentation scope
	 */
	public BytecodePool(Scope scope) {
		this.scope = scope;
		this.model = new ProjectModel().setLoader(this);
	}

	/**
	 * Adds a bytecode loader that can load bytecode using
	 * {@link ClassLoader#getResource(String)}
	 * 
	 * @param loader
	 * @return this for chaining
	 */
	public final BytecodePool addLoaderFor(ClassLoader loader) {
		addLoader(new ClassLoaderLoader(loader));
		return this;
	}

	/**
	 * Adds a bytecode loader that can load bytecode from the specified array
	 * 
	 * @param name
	 *            binary class name
	 * @param bytecode
	 *            bytecode
	 * @return this for chaining
	 */
	public final BytecodePool addLoaderFor(String name, byte[] bytecode) {
		addLoader(new MemoryLoader(name, bytecode));
		return this;
	}

	/**
	 * @return number of hits in bytecode cache
	 */
	public int getCacheHitCount() {
		return cache.getHitCount();
	}

	/**
	 * @return number of misses in bytecode cache
	 */
	public int getCacheMissCount() {
		return cache.getMissCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bytecode loadBytecode(String className) {
		Bytecode bytecode = cache.get(className);

		if (bytecode == null) {
			bytecode = super.loadBytecode(className);
			if (bytecode == null) {
				bytecode = NOT_FOUND;
			}
			cache.put(className, bytecode);
		}

		if (bytecode == NOT_FOUND) {
			return null;
		} else {
			return bytecode;
		}
	}

	/**
	 * Loads class
	 * 
	 * @param className
	 *            the name of the class
	 * @return loaded {@link Class}
	 * @throws ClassNotFoundException
	 */
	public Class<?> loadClass(final String className) throws ClassNotFoundException {
		ClassesUtil.checkClassNameArg(className);
		Bytecode bytecode = loadBytecode(className);
		if (bytecode == null) {
			throw new ClassNotFoundException(className);
		}
		return loadClass(className, bytecode.getBytes());
	}

	/**
	 * Loads specified bytecode into a {@link Class} object
	 * 
	 * @param className
	 *            binary class name
	 * @param bytecode
	 *            bytecode
	 * @return created {@link Class} object
	 */
	public Class<?> loadClass(final String className, byte[] bytecode) {
		return ClassesUtil.loadClass(className, bytecode);
	}

	/**
	 * Resets cache statistics
	 */
	public void resetCacheStatistics() {
		cache.resetStatistics();
	}

}
