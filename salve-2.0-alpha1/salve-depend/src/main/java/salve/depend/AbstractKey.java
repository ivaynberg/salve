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
import java.util.Arrays;

/**
 * Base class for dependency keys that implements {@link #equals(Object)},
 * {@link #hashCode()}, and {@link #toString()}
 * 
 * @author igor.vaynberg
 * 
 */
public abstract class AbstractKey implements Key {
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof Key) {
			Key other = (Key) obj;
			if (!getType().equals(other.getType())) {
				return false;
			}
			if (!getGenericType().equals(other.getGenericType())) {
				return false;
			}
			if (!Arrays.equals(getAnnotations(), other.getAnnotations())) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(getAnnotations());
		result = prime * result + (getGenericType() == null ? 0 : getGenericType().hashCode());
		result = prime * result + (getType() == null ? 0 : getType().hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append("[").append(getClass().getName()).append(" type=").append(getType().getName());
		str.append(" genericType=").append(getGenericType().toString());
		str.append(" annotations=[");
		int idx = 0;
		for (Annotation annot : getAnnotations()) {
			str.append(idx++ > 0 ? ", " : "").append(annot.toString());
		}
		str.append("]]");
		return str.toString();
	}
}
