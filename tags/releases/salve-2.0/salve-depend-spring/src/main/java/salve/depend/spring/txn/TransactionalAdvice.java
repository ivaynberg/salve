package salve.depend.spring.txn;

import salve.aop.MethodInvocation;
import salve.depend.DependencyLibrary;

/**
 * Transactional advice
 * 
 * @author igor.vaynberg
 */
public class TransactionalAdvice {

	public static Object transact(MethodInvocation invocation) throws Throwable {

		TransactionManager txnman = (TransactionManager) DependencyLibrary
				.locate(new TransactionalKey(invocation.getMethod()));

		TransactionAttribute attr = new TransactionAttribute(invocation
				.getMethod());

		Object txn = txnman.start(attr, invocation.getMethod().getName());
		try {
			Object ret = invocation.execute();
			txnman.finish(txn);
			return ret;
		} catch (Throwable e) {
			txnman.finish(e, txn);
			throw e;
		} finally {
			txnman.cleanup(txn);
		}
	}
}
