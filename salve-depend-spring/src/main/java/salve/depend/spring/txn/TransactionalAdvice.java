package salve.depend.spring.txn;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import salve.aop.MethodInvocation;
import salve.depend.DependencyLibrary;
import salve.depend.Key;

public class TransactionalAdvice {

	// TODO user TransactionalKey instead
	private static final Key TXNMAN_KEY = new Key() {
		public Class<?> getType() {
			return TransactionManager.class;
		}

		public Annotation[] getAnnotations() {
			return null;
		}

		public Type getGenericType() {

			return TransactionManager.class;
		}
	};

	public static Object transact(MethodInvocation invocation) throws Throwable {

		TransactionManager txnman = (TransactionManager) DependencyLibrary
				.locate(TXNMAN_KEY);

		TransactionAttribute attr = new TransactionAttribute(invocation
				.getMethod());

		Object txn = txnman.start(attr, invocation.getMethod().getName());
		try {
			Object ret = invocation.execute();
			txnman.finish(txn);
			return ret;
		} catch (Throwable e) {
			// TODO unwrap the invocation target exception automatically
			if (e instanceof InvocationTargetException) {
				e = e.getCause();
			}
			txnman.finish(e, txn);
			throw e;
		} finally {
			txnman.cleanup(txn);
		}
	}
}
