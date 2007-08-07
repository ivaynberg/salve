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
