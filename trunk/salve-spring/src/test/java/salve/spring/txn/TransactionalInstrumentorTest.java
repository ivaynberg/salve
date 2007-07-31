package salve.spring.txn;

import java.io.FileOutputStream;

import junit.framework.Assert;

import org.aopalliance.aop.Advice;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;

import salve.asm.loader.BytecodePool;
import salve.asm.loader.ClassLoaderLoader;
import salve.dependency.DependencyInstrumentorTest;
import salve.dependency.DependencyLibrary;
import salve.dependency.Locator;

public class TransactionalInstrumentorTest extends Assert {
	private static String BEAN_NAME = "salve/spring/txn/TransactionalMethodBean";
	private static Class<?> beanClass;

	private static PlatformTransactionManager ptm;
	private static TransactionAttributeSourceAdvisor adv;
	private static Locator locator;

	@BeforeClass
	public static void initClass() throws Exception {
		loadBeans();
		initDependencyLibrary();
	}

	private static void initDependencyLibrary() {
		DependencyLibrary.clear();

		ptm = EasyMock.createMock(PlatformTransactionManager.class);
		adv = new MockTransactionAttributeSourceAdvisor();
		locator = EasyMock.createMock(Locator.class);
		DependencyLibrary.addLocator(locator);
	}

	private static void loadBeans() throws Exception {
		ClassLoader classLoader = DependencyInstrumentorTest.class
				.getClassLoader();
		BytecodePool pool = new BytecodePool();
		pool.addLoader(new ClassLoaderLoader(classLoader));

		byte[] bytecode = pool.loadBytecode(BEAN_NAME);
		if (bytecode == null) {
			throw new RuntimeException("Could not load bytecode for "
					+ BEAN_NAME);
		}

		TransactionalInstrumentor inst = new TransactionalInstrumentor();
		bytecode = inst.instrument(classLoader, BEAN_NAME, bytecode);

		System.out.println("target/test-classes/" + BEAN_NAME
				+ "$Instrumented.class");
		FileOutputStream fos = new FileOutputStream("target/test-classes/"
				+ BEAN_NAME + "$Instrumented.class");
		fos.write(bytecode);
		fos.close();

		beanClass = pool.loadClass(BEAN_NAME, bytecode);

	}

	@Before
	public void initMocks() {
		EasyMock.reset(locator, ptm);
		EasyMock.expect(locator.locate(AdviserUtil.AdviserKey.INSTANCE))
				.andReturn(adv);
	}

	@Test
	public void testSimple() throws Exception {
		TransactionalMethodBean bean = (TransactionalMethodBean) beanClass
				.newInstance();

	}

	private static class MockTransactionSupport extends
			TransactionAspectSupport implements Advice {
		@Override
		public PlatformTransactionManager getTransactionManager() {
			return ptm;
		}
	}

	private static class MockTransactionAttributeSourceAdvisor extends
			TransactionAttributeSourceAdvisor {
		@Override
		public Advice getAdvice() {
			return new MockTransactionSupport();
		}
	}
}
