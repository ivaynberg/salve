package salve.depend.spring.txn;

public class DummyTransactionManager implements TransactionManager
{
	private boolean insideTransaction = false;

	public Object start(final TransactionAttribute attr, final String txnName)
	{
		insideTransaction = true;
		return null;
	}

	public void finish(final Object txn)
	{
	}

	public void finish(final Throwable ex, final Object txn)
	{
	}

	public void cleanup(final Object txn)
	{
		insideTransaction = false;
	}

	public boolean isInsideTransaction()
	{
		return insideTransaction;
	}
}
