package salve.spring.txn;

import java.lang.reflect.Method;

import org.springframework.transaction.interceptor.TransactionAspectSupport;

public class TransactionAspectSupportAdapter extends TransactionAspectSupport {
	public TransactionAspectSupportAdapter(TransactionAspectSupportAdapter base) {
		setTransactionAttributeSource(base.getTransactionAttributeSource());
		setTransactionManager(base.getTransactionManager());
	}

	@Override
	public void cleanupTransactionInfo(TransactionInfo txInfo) {
		super.cleanupTransactionInfo(txInfo);
	}

	@Override
	public void commitTransactionAfterReturning(TransactionInfo txInfo) {
		super.commitTransactionAfterReturning(txInfo);
	}

	@Override
	public void completeTransactionAfterThrowing(TransactionInfo txInfo,
			Throwable ex) {
		super.completeTransactionAfterThrowing(txInfo, ex);
	}

	@Override
	public TransactionInfo createTransactionIfNecessary(Method method,
			Class targetClass) {
		return super.createTransactionIfNecessary(method, targetClass);
	}

}
