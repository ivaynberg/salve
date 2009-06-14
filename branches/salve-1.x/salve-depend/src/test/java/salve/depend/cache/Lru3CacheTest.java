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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;

import salve.depend.Key;

public class Lru3CacheTest extends Assert {

	private static class K extends O implements Key {

		public K(int index) {
			super(index);
		}

		public Annotation[] getAnnotations() {
			return null;
		}

		public Type getGenericType() {
			return null;
		}

		public Class<?> getType() {
			return null;
		}

	}

	private static class O {
		private final int index;

		public O(int index) {
			this.index = index;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + String.valueOf(index);
		}

	}

	@Test
	public void test() {
		K k1 = new K(1);
		K k2 = new K(2);
		K k3 = new K(3);
		K k4 = new K(4);
		O o1 = new O(1);
		O o2 = new O(2);
		O o3 = new O(3);
		O o4 = new O(4);

		Lru3Cache<K, O> lru = new Lru3Cache<K, O>();

		// test addition to an empty cache
		assertTrue(lru.get(k1) == null);
		lru.put(k1, o1);
		assertTrue(lru.indexOf(k1) == 0);
		assertTrue(lru.get(k1) == o1);

		// k1

		// test additions to nonempty cache push down other entries
		lru.put(k2, o2);
		assertTrue(lru.indexOf(k2) == 0);
		assertTrue(lru.indexOf(k1) == 1);

		// k2,k1

		assertTrue(lru.get(k2) == o2);
		assertTrue(lru.get(k1) == o1);

		// k1,k2 - get(k1) puts k1 on top

		// test access puts entry on top
		assertTrue(lru.indexOf(k1) == 0);
		assertTrue(lru.indexOf(k2) == 1);

		lru.put(k3, o3);

		// k3,k1,k2
		assertTrue(lru.indexOf(k3) == 0);
		assertTrue(lru.indexOf(k1) == 1);
		assertTrue(lru.indexOf(k2) == 2);

		assertTrue(lru.get(k3) == o3);
		lru.get(k2);

		// k2,k3,k1
		assertTrue(lru.indexOf(k3) == 1);
		assertTrue(lru.indexOf(k1) == 2);
		assertTrue(lru.indexOf(k2) == 0);

		assertTrue(lru.get(k2) == o2);

		lru.put(k4, o4);
		// k4,k2,k3 - k1 is pushed out
		assertTrue(lru.indexOf(k4) == 0);
		assertTrue(lru.indexOf(k2) == 1);
		assertTrue(lru.indexOf(k3) == 2);
		assertTrue(lru.indexOf(k1) < 0);

		assertTrue(lru.get(k4) == o4);
		assertTrue(lru.get(k2) == o2);
		assertTrue(lru.get(k3) == o3);

	}

}
