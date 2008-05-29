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
package salve.depend.guice;

import java.lang.annotation.Annotation;

import salve.depend.DependencyLibrary;
import salve.depend.Key;
import salve.depend.Locator;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Salve locator that can connect {@link DependencyLibrary} to guice
 * {@link Injector}.
 * <p>
 * NOTE It is recommended to install this locator as the last in the chain
 * because of guice's implicit-binding concept which makes it difficult to
 * properly return null from {@link #locate(Key)} when no binding for specified
 * type has been created
 * </p>
 * 
 * @author ivaynberg
 * 
 */
public class GuiceBeanLocator implements Locator {
	private final Injector injector;

	/**
	 * @param injector
	 *            guice injector
	 */
	public GuiceBeanLocator(final Injector injector) {
		super();
		this.injector = injector;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object locate(Key key) {
		Annotation bindingAnnot = null;

		Annotation[] annots = key.getAnnotations();
		for (Annotation annot : annots) {
			if (annot.annotationType().getAnnotation(BindingAnnotation.class) != null) {
				bindingAnnot = annot;
				break;
			}
		}

		com.google.inject.Key<?> guiceKey = null;

		if (bindingAnnot == null) {
			guiceKey = com.google.inject.Key.get(key.getType());
		} else {
			guiceKey = com.google.inject.Key.get(key.getType(), bindingAnnot);
		}

		if (Provider.class.isAssignableFrom(key.getType())) {
			return injector.getProvider(guiceKey);
		} else {
			return injector.getInstance(guiceKey);
		}

		// XXX investigate properly returning null, see javadoc note
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("[%s injector=%s]", getClass().getName(), injector
				.toString());
	}

}
