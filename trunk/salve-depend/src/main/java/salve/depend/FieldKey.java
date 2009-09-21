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
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Dependency {@link Key} based on a {@link Field} that represents the
 * dependency.
 * 
 * Default {@link Key} implementation used by Salve instrumentors
 * 
 * @author ivaynberg
 */
public class FieldKey extends AbstractKey implements Key {
	private static final long serialVersionUID = 1L;
	private final Field field;

	/**
	 * Constructor
	 * 
	 * @param fieldOwner
	 *            type that holds the instrumented key field
	 * @param fieldName
	 *            name of instrumented key field
	 */
	public FieldKey(Class<?> fieldOwner, String fieldName) {

		try {
			field = fieldOwner.getDeclaredField(fieldName);
		} catch (Exception e) {
			throw new RuntimeException("Could not build Key based on field: " + fieldOwner + "#" + fieldName, e);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param field
	 *            field
	 */
	public FieldKey(Field field) {

		this.field = field;
	}

	/**
	 * {@inheritDoc}
	 */
	public Annotation[] getAnnotations() {
		return field.getAnnotations();
	}

	/**
	 * {@inheritDoc}
	 */
	public Type getGenericType() {
		return field.getGenericType();
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public String toString() {

		return "[" + getClass().getSimpleName() + " field=" + field.getDeclaringClass().getName() + "#"
				+ field.getName() + "]";
	}
}
