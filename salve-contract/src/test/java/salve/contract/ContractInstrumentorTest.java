package salve.contract;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import salve.loader.BytecodePool;
import salve.loader.ClassLoaderLoader;

public class ContractInstrumentorTest extends Assert {
	private static final String BEAN_NAME = "salve/contract/Bean";
	private static Class<?> beanClass;
	private Bean bean;

	@Before
	public void createBean() throws Exception {
		bean = (Bean) beanClass.newInstance();
	}

	@Test
	public void testNotNull() {
		Object token = new Object();
		assertTrue(token == bean.testNotNull(token));
		try {
			bean.testNotNull(null);
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			bean.testNotNull(Bean.NULL);
			fail("Expected " + IllegalStateException.class.getName());
		} catch (IllegalStateException e) {
			// expected
		}
	}

	@BeforeClass
	public static void init() throws Exception {
		loadBeans();
	}

	private static void loadBeans() throws Exception {
		ClassLoader classLoader = ContractInstrumentorTest.class
				.getClassLoader();
		BytecodePool pool = new BytecodePool();
		pool.addLoader(new ClassLoaderLoader(classLoader));
		beanClass = pool.instrumentIntoClass(BEAN_NAME,
				new ContractInstrumentor());

		// FileOutputStream fos = new FileOutputStream(
		// "target/test-classes/salve/contract/Bean$Instrumented.class");
		// fos.write(bytecode);
		// fos.close();
	}

}
