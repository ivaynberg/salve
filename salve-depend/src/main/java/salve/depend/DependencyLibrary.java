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

import salve.depend.cache.Lru3Cache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dependency library is a singleton that holds all registered locators. Users
 * use this singleton to register their locators, and Salve uses it to retrieve
 * dependencies from those locators.
 *
 * @author ivaynberg
 */
public class DependencyLibrary {
	private static final List<Locator> locators = new CopyOnWriteArrayList<Locator>();
	private static CacheProvider cacheProvider = new CacheProvider() {

		@Override
		public Cache<Key, Object> getCache() {
			return new Lru3Cache<Key, Object>();
		}

	};

	private static final ThreadLocal<Cache<Key, Object>> LRU = new ThreadLocal<Cache<Key, Object>>() {

		@Override
		public Cache<Key, Object> get() {
			// using initialValue() would be better but then we need ThreadLocal.remove() and therefore
			// advanced mode when using retrotranslator
			Cache<Key, Object> value = super.get();

			if(value == null)
				set(value = cacheProvider.getCache());

			return value;
		}

	};

	/**
	 * Registers a locator
	 *
	 * @param locator
	 */
	public static void addLocator(Locator locator) {
		locators.add(locator);
	}

	/**
	 * Clears all registered locators
	 */
	public static void clear() {
		locators.clear();
		// TODO there is no way to clear all the values from all threads...
		// use set(null) instead of remove() which is @since 1.5:
		// this enables retrotranslation without using mode = "advanced"
		// (and probably makes the resulting bytecode a little safer)
		// LRU.remove();
		LRU.set(null);
	}

	/**
	 * Attempts to find a dependency using registered locators and the specified
	 * dependency key
	 *
	 * @param key
	 * @return located dependency
	 * @throws DependencyNotFoundException
	 *             when dependency is not found in any registered locator
	 */
	public static Object locate(Key key) throws DependencyNotFoundException {
		if (key == null) {
			throw new IllegalArgumentException("Argument `key` cannot be null");
		}

		Object dependency = LRU.get().get(key);
		if (dependency != null) {
			return dependency;
		}

		for (Locator locator : locators) {
			dependency = locator.locate(key);
			if (dependency != null) {
				checkType(dependency, key, locator);
				LRU.get().put(key, dependency);
				return dependency;
			}
		}

		throw new DependencyNotFoundException(key);
	}

	/**
	 * Sets cache provider. The cache is used in {@link #locate(Key)} before the
	 * locators are searched.
	 *
	 * @param cacheProvider
	 */
	public static void setCacheProvider(CacheProvider cacheProvider) {
		DependencyLibrary.cacheProvider = cacheProvider;
	}

	/**
	 * Checks the type of located dependency against the type specified by the
	 * key
	 *
	 * @param dependency
	 * @param key
	 * @param locator
	 * @throws IllegalStateException
	 *             if types do not match
	 */
	private static void checkType(Object dependency, Key key, Locator locator) {
		final Class<?> locatedType = dependency.getClass();
		final Class<?> requiredType = key.getType();
		if (!requiredType.isAssignableFrom(locatedType)) {
			throw new IllegalStateException(String.format("Locator returned dependency of invalid type. "
					+ "Located type: %s. Required type: %s. " + "Key: %s. Locator: %s", locatedType, requiredType, key,
					locator));
		}
	}
}
