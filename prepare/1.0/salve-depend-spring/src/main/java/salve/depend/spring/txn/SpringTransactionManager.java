package salve.depend.spring.txn;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;

public class SpringTransactionManager implements TransactionManager,
		ApplicationContextAware, InitializingBean {

	private TransactionAspectSupport transactionSupport;
	private ApplicationContext applicationContext;

	// cache of method lookups
	private Method CREATE = null;
	private Method COMMIT = null;
	private Method COMMIT_AFTER_ERR = null;
	private Method CLEANUP = null;

	public void cleanup(Object txn) {
		try {
			CLEANUP.invoke(transactionSupport, txn);
		} catch (Exception e) {
			throw new RuntimeException(
					"Salve failed to cleanup transaction info", e);
		}
	}

	public void finish(Object txn) {
		try {
			COMMIT.invoke(transactionSupport, txn);
		} catch (Exception e) {
			throw new RuntimeException("Salve failed to complete transaction",
					e);
		}
	}

	public void finish(Throwable ex, Object txn) {
		try {
			COMMIT_AFTER_ERR.invoke(transactionSupport, txn, ex);
		} catch (Exception e) {
			throw new RuntimeException(
					"Salve failed to complete transaction on error", e);
		}
	}

	public Object start(TransactionAttribute attr, String txnName) {
		try {
			return CREATE.invoke(transactionSupport, attr, txnName);
		} catch (Exception e) {
			throw new RuntimeException("Salve failed to create transaction", e);
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

	}

	public void afterPropertiesSet() throws Exception {

		TransactionAttributeSourceAdvisor adviser = (TransactionAttributeSourceAdvisor) BeanFactoryUtils
				.beanOfTypeIncludingAncestors(applicationContext,
						TransactionAttributeSourceAdvisor.class);

		if (adviser == null) {
			throw new IllegalStateException(
					"Could not find a bean of type: "
							+ TransactionAttributeSourceAdvisor.class.getName()
							+ " in application context. Make sure you have <tx:annotation-driven/> element in your spring config xml file.");
		}

		// XXX HACK HACK HACK
		transactionSupport = (TransactionAspectSupport) adviser.getAdvice();

		initializeMethodCache();

	}

	private void initializeMethodCache() {
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

}
