package salve.depend.spring.txn;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * Bridge between salve's {@link TransactionManager} aspect and spring's
 * {@link PlatformTransactionManager}
 * <p>
 * The spring context must contain an instance of this bean.
 * 
 * @author igor.vaynberg
 */
public class SpringTransactionManager extends TransactionAspectSupport
		implements TransactionManager, InitializingBean {

	public void cleanup(Object txn) {
		super.cleanupTransactionInfo((TransactionInfo) txn);
	}

	public void finish(Object txn) {
		super.commitTransactionAfterReturning((TransactionInfo) txn);
	}

	public void finish(Throwable ex, Object txn) {
		super.completeTransactionAfterThrowing((TransactionInfo) txn, ex);
	}

	public Object start(TransactionAttribute attr, String txnName) {
		return super.createTransactionIfNecessary(determineTransactionManager(attr), attr, txnName);
	}

	public void afterPropertiesSet() {
		setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
	}

}
