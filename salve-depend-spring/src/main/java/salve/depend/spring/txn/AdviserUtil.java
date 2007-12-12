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
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;

import salve.depend.DependencyLibrary;
import salve.depend.Key;

/**
 * INTERNAL
 * <p>
 * Utility library used by instrumented bytecode additions to lookup and use
 * {@link PlatformTransactionManager} associated with
 * {@code <tx:annotation-driven/>} tag in spring's application context.
 * </p>
 * 
 * @author ivaynberg
 */
public class AdviserUtil {
	private static final Method CREATE;
	private static Method COMMIT = null;
	private static Method COMMIT_AFTER_ERR = null;
	private static Method CLEANUP = null;

	static {
		try {
			CREATE = TransactionAspectSupport.class
					.getDeclaredMethod(
							"createTransactionIfNecessary",
							new Class[] {
									org.springframework.transaction.interceptor.TransactionAttribute.class,
									String.class });

			Method[] methods = TransactionAspectSupport.class
					.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals("commitTransactionAfterReturning")) {
					COMMIT = method;
				} else if (method.getName().equals(
						"completeTransactionAfterThrowing")) {
					COMMIT_AFTER_ERR = method;
				} else if (method.getName().equals("cleanupTransactionInfo")) {
					CLEANUP = method;
				}
			}

			if (COMMIT == null) {
				throw new RuntimeException(
						"COULD NOT FIND COMMIT METHOD IN TRANSACTION ASPECT SUPPORT");
			}
			if (COMMIT_AFTER_ERR == null) {
				throw new RuntimeException(
						"COULD NOT FIND COMMIT_AFTER_ERR METHOD IN TRANSACTION ASPECT SUPPORT");
			}
			if (CLEANUP == null) {
				throw new RuntimeException(
						"COULD NOT FIND CLEANUP METHOD IN TRANSACTION ASPECT SUPPORT");
			}

			CREATE.setAccessible(true);
			COMMIT.setAccessible(true);
			COMMIT_AFTER_ERR.setAccessible(true);
			CLEANUP.setAccessible(true);

		} catch (Exception e) {
			throw new RuntimeException("COULD NOT INITIALIZE ADVISER UTIL!", e);
		}
	}

	private AdviserUtil() {

	}

	public static Object begin(TransactionAttribute attr, String txnName) {
		//System.out.println("ADVISER UTIL::BEGIN");
		try {
			return CREATE.invoke(getSupport(), attr, txnName);
		} catch (Exception e) {
			throw new RuntimeException("Salve failed to create transaction", e);
		}
	}

	public static void finish(Object txn) {
		//System.out.println("ADVISER UTIL::FINISH");
		try {
			COMMIT.invoke(getSupport(), txn);
		} catch (Exception e) {
			throw new RuntimeException("Salve failed to complete transaction",
					e);
		}
	}

	public static void finish(Throwable ex, Object txn) {
		//System.out.println("ADVISER UTIL::FINISH(EX)");
		try {
			COMMIT_AFTER_ERR.invoke(getSupport(), txn, ex);
		} catch (Exception e) {
			throw new RuntimeException(
					"Salve failed to complete transaction on error", e);
		}
	}

	public static void cleanup(Object txn) {
		//System.out.println("ADVISER UTIL::CLEANUP");
		try {
			CLEANUP.invoke(getSupport(), txn);
		} catch (Exception e) {
			throw new RuntimeException(
					"Salve failed to cleanup transaction info", e);
		}
	}

	private static TransactionAspectSupport getSupport() {
		// XXX better exception handling - wrap dependency not found with a
		// nice message
		TransactionAttributeSourceAdvisor adviser = (TransactionAttributeSourceAdvisor) DependencyLibrary
				.locate(AdviserKey.INSTANCE);

		// XXX HACK HACK HACK
		TransactionAspectSupport base = (TransactionAspectSupport) adviser
				.getAdvice();

		return base;
	}

	public static PlatformTransactionManager locateTransactionManager() {
		// XXX better exception handling - wrap dependency not found with a
		// nice message
		TransactionAttributeSourceAdvisor adviser = (TransactionAttributeSourceAdvisor) DependencyLibrary
				.locate(AdviserKey.INSTANCE);

		// XXX HACK HACK HACK
		TransactionAspectSupport base = (TransactionAspectSupport) adviser
				.getAdvice();

		return base.getTransactionManager();
	}

	static class AdviserKey implements Key {

		public static final AdviserKey INSTANCE = new AdviserKey();

		private static final long serialVersionUID = 1L;

		private static final Annotation[] EMPTY = new Annotation[] {};

		private AdviserKey() {

		}

		public Annotation[] getAnnotations() {
			return EMPTY;
		}

		public Class<?> getType() {
			return TransactionAttributeSourceAdvisor.class;
		}

		@Override
		public String toString() {
			return "{" + getClass().getName() + "}";
		}
	}

}
