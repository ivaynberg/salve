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
package salve.depend.cache;

import salve.depend.Cache;

/**
 * Noop cache for testing. Some tests set up mock locators, and are interested
 * in checking how many times the locate method is called - a cache screws up
 * that count, so those tests need a way to disable the cache.
 * 
 * @author ivaynberg
 * 
 * @see NoopCacheProvider
 * 
 * @param <K>
 * @param <V>
 */
public class NoopCache<K, V> implements Cache<K, V> {

	public void clear() {
	}

	public V get(K key) {
		return null;
	}

	public void put(K key, V value) {
	}

}
