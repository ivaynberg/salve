package salve.depend.spring.txn;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Mock for {@link TransactionStatus}
 * 
 * @author igor.vaynberg
 */
class TransactionStatusMock implements TransactionStatus {

	public Object createSavepoint() throws TransactionException {

		return null;
	}

	public boolean hasSavepoint() {

		return false;
	}

	public boolean isCompleted() {

		return false;
	}

	public boolean isNewTransaction() {

		return false;
	}

	public boolean isRollbackOnly() {

		return false;
	}

	public void releaseSavepoint(Object savepoint)
			throws TransactionException {

	}

	public void rollbackToSavepoint(Object savepoint)
			throws TransactionException {

	}

	public void setRollbackOnly() {

	}

}