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
package salve.depend.spring.txn;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

import org.springframework.transaction.annotation.Transactional;

import salve.depend.AbstractKey;
import salve.depend.Key;

/**
 * INTERNAL
 * <p>
 * {@link Key} used to lookup a transaction manager for a specific method
 * </p>
 * 
 * @author ivaynberg
 */
public class TransactionalKey extends AbstractKey {
	private static final long serialVersionUID = 1L;
	private final Annotation[] annots;

	/**
	 * Constructor
	 * 
	 * @param clazz
	 *            class that contains the method annotated with
	 *            {@link Transactional}
	 * @param methodName
	 *            name of annotated method
	 * @param methodArgTypes
	 *            array of annotated method's argument types
	 */
	public TransactionalKey(Class<?> clazz, String methodName,
			Class<?>[] methodArgTypes) {
		try {
			AccessibleObject ao;
			if ("<init>".equals(methodName)) {
				ao = clazz.getDeclaredConstructor(methodArgTypes);
			} else {
				ao = clazz.getDeclaredMethod(methodName, methodArgTypes);
			}
			Annotation[] annots = ao.getAnnotations();
			this.annots = new Annotation[annots.length];
			System.arraycopy(annots, 0, this.annots, 0, annots.length);
		} catch (Exception e) {
			throw new RuntimeException(
					String
							.format(
									"Error while attempting to retrieve method annotations from method %s.%s()",
									clazz.getName(), methodName), e);
		}
	}

	/** {@inheritDoc} */
	public Annotation[] getAnnotations() {
		return annots;
	}

	/** {@inheritDoc} */
	public Type getGenericType() {
		return TransactionManager.class;
	}

	/** {@inheritDoc} */
	public Class<?> getType() {
		return TransactionManager.class;
	}

}
