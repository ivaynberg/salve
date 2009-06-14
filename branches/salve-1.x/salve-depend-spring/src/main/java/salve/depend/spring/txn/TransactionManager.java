package salve.depend.spring.txn;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import salve.depend.Key;

public interface TransactionManager {
	Object start(TransactionAttribute attr, String txnName);

	void finish(Object txn);

	void finish(Throwable ex, Object txn);

	void cleanup(Object txn);

}
