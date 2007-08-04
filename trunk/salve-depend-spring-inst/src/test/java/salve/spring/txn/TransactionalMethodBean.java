package salve.spring.txn;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalMethodBean {
	public static long CLINIT_FORCER = System.currentTimeMillis();

	@Transactional
	public void args1(int a) {

	}

	@Transactional
	public Object args2(int mode, Object[] a, double[] b) {
		switch (mode) {
		case 1:
			return a;
		case 2:
			return b;
		default:
			return null;
		}
	}

	@Transactional
	public Object exception(int mode, Object p) {
		switch (mode) {
		case 1:
			throw new ArrayIndexOutOfBoundsException();
		case 2:
			throw new IllegalStateException();
		default:
			return p;
		}
	}

	@Transactional
	public Object ret(Object p) {
		return p;
	}

	@Transactional
	public void simple() {

	}

}
