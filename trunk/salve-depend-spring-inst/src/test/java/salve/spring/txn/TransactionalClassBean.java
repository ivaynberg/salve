package salve.spring.txn;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class TransactionalClassBean {
	public static long CLINIT_FORCER = System.currentTimeMillis();

	public void method() {

	}
}
