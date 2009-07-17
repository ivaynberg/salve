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
package salve.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MetaData implements Serializable {

	public static class Key<T> implements Serializable {

		@Override
		public boolean equals(Object obj) {
			return obj != null && getClass().isInstance(obj);
		}
	}

	private final Map<Key<?>, Object> map = new HashMap<Key<?>, Object>();

	public boolean contains(final Key<?> key) {
		return map.containsKey(key);
	}

	public <T> T get(final Key<T> key) {
		return (T) map.get(key);
	}

	public <T> void put(final Key<T> key, final T object) {
		map.put(key, object);
	}
}
