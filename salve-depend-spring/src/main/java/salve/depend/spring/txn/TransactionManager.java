package salve.depend.spring.txn;

import java.lang.annotation.Annotation;

import salve.depend.Key;

public interface TransactionManager {
	Object start(TransactionAttribute attr, String txnName);

	void finish(Object txn);

	void finish(Throwable ex, Object txn);

	void cleanup(Object txn);

	public static final Key KEY = new Key() {

		public Annotation[] getAnnotations() {
			return null;
		}

		public Class<?> getType() {
			return TransactionManager.class;
		}
	};
}
