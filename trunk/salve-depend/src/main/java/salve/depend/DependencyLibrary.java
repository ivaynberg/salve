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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dependency library is a singleton that holds all registered locators. Users
 * use this singleton to register their locators, and Salve uses it to retrieve
 * dependencies from those locators.
 * 
 * @author ivaynberg
 * 
 */
public class DependencyLibrary {
	private static final List<Locator> locators = new CopyOnWriteArrayList<Locator>();

	/**
	 * Registers a locator
	 * 
	 * @param locator
	 */
	public static final void addLocator(Locator locator) {
		locators.add(locator);
	}

	/**
	 * Clears all registered locators
	 */
	public static final void clear() {
		locators.clear();
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
	public static final Object locate(Key key) throws DependencyNotFoundException {
		for (Locator locator : locators) {
			Object dependency = locator.locate(key);
			if (dependency != null) {
				checkType(dependency, key, locator);
				return dependency;
			}
		}
		throw new DependencyNotFoundException(key);
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
