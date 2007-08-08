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

import java.lang.reflect.AccessibleObject;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

public class TransactionAttribute extends RuleBasedTransactionAttribute {
	public TransactionAttribute(Class clazz, String methodName,
			Class[] methodArgTypes) {
		try {
			AccessibleObject ao;
			if ("<init>".equals(methodName)) {
				ao = clazz.getDeclaredConstructor(methodArgTypes);
			} else {
				ao = clazz.getDeclaredMethod(methodName, methodArgTypes);
			}
			SpringTransactional t = ao.getAnnotation(SpringTransactional.class);
			if (t == null) {
				t = (SpringTransactional) clazz
						.getAnnotation(SpringTransactional.class);
				if (t == null) {

					throw new IllegalStateException(
							String
									.format(
											"Instrumented method nor class %s.%s() contains %s annotation",
											clazz.getName(), methodName,
											Transactional.class.getName()));
				}

			}
			init(t);
		} catch (Exception e) {
			throw new RuntimeException(
					String
							.format(
									"Error while attempting to retrieve %s annotation from method %s.%s()",
									Transactional.class.getName(), clazz
											.getName(), methodName), e);
		}
	}

	public TransactionAttribute(SpringTransactional transactional) {
		init(transactional);
	}

	@SuppressWarnings("unchecked")
	private void init(SpringTransactional t) {
		setPropagationBehavior(t.propagation().value());
		setIsolationLevel(t.isolation().value());
		setTimeout(t.timeout());
		setReadOnly(t.readOnly());
		for (String cn : t.noRollbackForClassName()) {
			getRollbackRules().add(new NoRollbackRuleAttribute(cn));
		}
		for (Class c : t.noRollbackFor()) {
			getRollbackRules().add(new NoRollbackRuleAttribute(c));
		}
		for (String cn : t.rollbackForClassName()) {
			getRollbackRules().add(new RollbackRuleAttribute(cn));
		}
		for (Class c : t.rollbackFor()) {
			getRollbackRules().add(new RollbackRuleAttribute(c));
		}
	}
}
