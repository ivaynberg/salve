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

import java.lang.reflect.Field;

/**
 * Thrown when a user tries to write into the dependency field that has been
 * instrumented.
 * 
 * @author ivaynberg
 * 
 */
public class IllegalFieldWriteException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public IllegalFieldWriteException(Field field) {
		this(field.getDeclaringClass().getName(), field.getName());
	}

	public IllegalFieldWriteException(String clazz, String field) {
		super("Attempted to write to field `" + field + "` that has been removed from class `" + clazz
				+ "` by salve's dependency instrumentor");
	}
}
