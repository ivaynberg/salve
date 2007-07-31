package salve.spring.txn;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalMethodBean {
	@Transactional
	public void simple() {

	}

	@Transactional
	public void args1(int a) {

	}

	@Transactional
	public void args2(Object a, double b) {

	}

	public Object ret(Object p) {
		return p;
	}

	public Object exception(int kind, Object p) {
		if (kind == 1) {
			throw new ArrayIndexOutOfBoundsException();
		} else if (kind == 2) {
			throw new IllegalStateException();
		}
		return p;
	}

}
