package salve.spring.txn;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

public class Tester {
	private static final TransactionAttribute _salvestxn$attr0 = new TransactionAttribute(
			Tester.class, "simple", new Class[] { Integer.class, int.class });

	@Transactional
	public void simple(Integer a) {
		PlatformTransactionManager ptm = AdviserUtil.locateTransactionManager();
		TransactionStatus status = ptm.getTransaction(_salvestxn$attr0);

		AdviserUtil.complete(ptm, status, _salvestxn$attr0);
	}

}
