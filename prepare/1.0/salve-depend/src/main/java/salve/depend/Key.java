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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A key that can uniquely identify a dependency.
 * 
 * It is important to properly implement key equality, thus most implementations
 * should extend {@link AbstractKey} which provides proper
 * {@link #equals(Object)} and {@link #hashCode()} implementations.
 * 
 * @see Locator
 * @see DependencyLibrary
 * @see AbstractKey
 * 
 * @author ivaynberg
 */
public interface Key {
	/**
	 * @return annotations that identify the dependency
	 */
	Annotation[] getAnnotations();

	/**
	 * @return a {@link Type} object that represents the declared dependency
	 *         type.
	 */
	Type getGenericType();

	/**
	 * @return dependency {@link Class}
	 */
	Class<?> getType();
}
