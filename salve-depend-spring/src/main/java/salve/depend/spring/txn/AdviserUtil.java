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
package salve.depend.spring.txn;

import java.lang.annotation.Annotation;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;

import salve.depend.DependencyLibrary;
import salve.depend.Key;

public class AdviserUtil {
	private AdviserUtil() {

	}

	public static void complete(PlatformTransactionManager mgr,
			TransactionStatus st, TransactionAttribute attr) {
		mgr.commit(st);
	}

	public static void complete(Throwable t, PlatformTransactionManager mgr,
			TransactionStatus st, TransactionAttribute attr) {
		if (attr.rollbackOn(t)) {
			// XXX wrap any rte from rollback() nicely so we can still see
			// original error in param 't'
			mgr.rollback(st);
		} else {
			mgr.commit(st);
		}
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
