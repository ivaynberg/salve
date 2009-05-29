package salve.depend.spring.txn;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;
import salve.Scope;
import salve.depend.DependencyLibrary;
import salve.depend.Key;
import salve.depend.Locator;
import salve.depend.cache.NoopCacheProvider;
import salve.loader.BytecodePool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionalMethodTest extends Assert
{
	private static String METHODBEAN_NAME = "salve/depend/spring/txn/MethodBean";

	private TestTransactionManager transactionManager;
	private static Class<?> testClass;

	@BeforeClass
	public static void initClass() throws Exception
	{
		final ClassLoader loader = TestTransactionManager.class.getClassLoader();
		final BytecodePool pool = new BytecodePool(Scope.ALL).addLoaderFor(loader);
		final TransactionalInstrumentor inst = new TransactionalInstrumentor();
		testClass = pool.instrumentIntoClass(METHODBEAN_NAME, inst);
	}

	@Before
	public void init()
	{
		transactionManager = new TestTransactionManager();

		DependencyLibrary.clear();
		DependencyLibrary.setCacheProvider(new NoopCacheProvider());
		DependencyLibrary.addLocator(new Locator()
		{
			public Object locate(final Key key)
			{
				if (TransactionManager.class.equals(key.getType()))
					return transactionManager;

				return null;
			}
		});
	}

	@Test
	public void testMethodByExplicitCall() throws IllegalAccessException, InstantiationException
	{
		((MethodBean) testClass.newInstance()).testTransactionIsWorking(transactionManager);
	}

	@Test
	public void testMethodByAnnotationLocation() throws IllegalAccessException, InstantiationException, InvocationTargetException
	{
		final MethodBean mb = (MethodBean) testClass.newInstance();

		for (Method method : testClass.getMethods())
			if (method.isAnnotationPresent(MethodIndicator.class))
				method.invoke(mb, transactionManager);
	}

	public static class TestTransactionManager implements TransactionManager
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
}
