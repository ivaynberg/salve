/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package salve.depend.spring;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import salve.depend.Key;
import salve.depend.Locator;

/**
 * Implementation of {@link Locator} that looks up dependencies from spring's
 * {@link ApplicationContext}
 * 
 * @author ivaynberg
 */
public class SpringBeanLocator implements Locator {
	private final ApplicationContext context;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            application context
	 */
	public SpringBeanLocator(final ApplicationContext context) {
		super();
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Object locate(Key key) {

		if (key == null) {
			throw new IllegalArgumentException("Argument key cannot be null");
		}
		SpringBeanId id = null;
		// XXX refactor the loop into KeyUtils
		if (key.getAnnotations() != null) {
			for (Annotation annot : key.getAnnotations()) {
				if (SpringBeanId.class.equals(annot.annotationType())) {
					id = (SpringBeanId) annot;
				}
			}
		}

		if (id != null) {
			return context.getBean(id.value(), key.getType());
		}

		Map<String, Object> beans = context.getBeansOfType(key.getType());
		if (beans.size() == 1) {
			return beans.values().iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("[%s context=%s]", getClass().getName(), context
				.getDisplayName());
	}

}
