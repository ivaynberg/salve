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
import salve.depend.Key;

/**
 * Small three item LRU cache for {@literal key->dependency}. This cache
 * compares by instance equality of the key, which should be OK because most
 * keys are created as {@code private static final} constants by
 * instrumentation.
 * 
 * @author ivaynberg
 * 
 */
@SuppressWarnings("unchecked")
public class Lru3Cache<K, V> implements Cache<K, V> {
	private final K[] keys = (K[]) new Object[3];
	private final V[] values = (V[]) new Object[3];

	/*
	 * (non-Javadoc)
	 * 
	 * @see salve.depend.Cache#clear()
	 */
	public void clear() {
		keys[0] = null;
		keys[1] = null;
		keys[2] = null;
		values[0] = null;
		values[1] = null;
		values[2] = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see salve.depend.Cache#get(K)
	 */
	public V get(final K key) {
		if (keys[0] == key) {
			return values[0];
		}
		if (keys[1] == key) {
			swap(0, 1);
			return values[0];
		}
		if (keys[2] == key) {
			swap(0, 2);
			swap(1, 2);
			return values[0];
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see salve.depend.Cache#put(K, V)
	 */
	public void put(final K key, final V value) {
		swap(1, 2);
		swap(1, 0);
		keys[0] = key;
		values[0] = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(getClass().getSimpleName());
		sb.append(" keys[");
		for (K k : keys) {
			sb.append(k).append(" ");
		}
		sb.append("] objects[");
		for (V o : values) {
			sb.append(o).append(" ");
		}
		sb.append("]]");
		return sb.toString();

	}

	/**
	 * Looks up index of key in the array
	 * 
	 * @param key
	 * @return index of key in the array or -1 if not in the array
	 */
	int indexOf(final Key key) {
		if (keys[0] == key) {
			return 0;
		}
		if (keys[1] == key) {
			return 1;
		}
		if (keys[2] == key) {
			return 2;
		}
		return -1;
	}

	/**
	 * Swaps two elements
	 * 
	 * @param a
	 * @param b
	 */
	private void swap(final int a, final int b) {
		final K key = keys[a];
		final V value = values[a];
		keys[a] = keys[b];
		values[a] = values[b];
		keys[b] = key;
		values[b] = value;
	}

}
