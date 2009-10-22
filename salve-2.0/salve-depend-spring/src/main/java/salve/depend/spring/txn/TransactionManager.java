package salve.depend.spring.txn;


public interface TransactionManager {
	Object start(TransactionAttribute attr, String txnName);

	void finish(Object txn);

	void finish(Throwable ex, Object txn);

	void cleanup(Object txn);
}
