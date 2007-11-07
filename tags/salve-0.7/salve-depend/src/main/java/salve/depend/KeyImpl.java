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
import java.util.Arrays;

/**
 * Default {@link Key} implementation used by Salve instrumentors
 * 
 * This key implementation works by reading all the necessary information from a
 * special key field created by the Salve's DependencyInstrumentor
 * 
 * @author ivaynberg
 */
public class KeyImpl implements Key {
	private static final long serialVersionUID = 1L;
	private static final Annotation[] EMPTY = new Annotation[0];
	private final Class<?> type;

	private final Annotation[] annots;

	/**
	 * Constructor for dependencies that can be identified by their type only
	 * 
	 * @param type
	 */
	public KeyImpl(Class<?> type) {
		super();
		this.type = type;
		annots = EMPTY;
	}

	/**
	 * Constructor for dependencies that can be identified by their type and
	 * some additional annotations
	 * 
	 * @param type
	 * @param annots
	 */
	public KeyImpl(Class<?> type, Annotation[] annots) {
		super();
		this.type = type;
		this.annots = annots;
	}

	/**
	 * INTERNAL
	 * 
	 * Constructor used by DependencyInstrumentor
	 * 
	 * @param dependencyType
	 *            type of dependency
	 * @param keyOwner
	 *            type that holds the instrumented key field
	 * @param keyFieldName
	 *            name of instrumented key field
	 */
	public KeyImpl(Class<?> dependencyType, Class<?> keyOwner, String keyFieldName) {

		this.type = dependencyType;
		Field field;
		try {
			field = keyOwner.getDeclaredField(keyFieldName);
			annots = field.getAnnotations();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof KeyImpl) {
			KeyImpl other = (KeyImpl) obj;
			return type.equals(other.type) && Arrays.equals(annots, other.annots);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Annotation[] getAnnotations() {
		return annots;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return type.hashCode() + 37 * annots.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("[%s type=%s annots=%s]", getClass().getName(), type.getName(), annots);
	}
}
