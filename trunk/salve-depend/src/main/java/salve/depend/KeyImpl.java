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

public class KeyImpl implements Key {
	private static final long serialVersionUID = 1L;
	private static final Annotation[] EMPTY = new Annotation[0];
	private final Class<?> type;

	private final Annotation[] annots;

	public KeyImpl(Class<?> type) {
		super();
		this.type = type;
		annots = EMPTY;
	}

	public KeyImpl(Class<?> type, Annotation[] annots) {
		super();
		this.type = type;
		this.annots = annots;
	}

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

	@Override public boolean equals(Object obj) {
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

	public Annotation[] getAnnotations() {
		return annots;
	}

	public Class<?> getType() {
		return type;
	}

	@Override public int hashCode() {
		return type.hashCode() + 37 * annots.hashCode();
	}

	@Override public String toString() {
		return String.format("[%s type=%s annots=%s]", getClass().getName(), type.getName(), annots);
	}
}
