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

public interface Cache<K, V> {

	public abstract void clear();

	/**
	 * Retrieves object from cache
	 * 
	 * @param key
	 * @return object instance if it is in cache, null otherwise
	 */
	public abstract V get(final K key);

	/**
	 * Put the object into the cache. The object is put in the first place and
	 * any existing objects are shifted toward the end to make room.
	 * 
	 * @param key
	 * @param values
	 */
	public abstract void put(final K key, final V value);

}