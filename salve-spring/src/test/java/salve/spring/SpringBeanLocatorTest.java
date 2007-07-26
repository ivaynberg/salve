package salve.spring;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import salve.asm.loader.BytecodePool;
import salve.asm.loader.ClassLoaderLoader;
import salve.dependency.DependencyInstrumentor;
import salve.dependency.DependencyLibrary;
import salve.dependency.DependencyNotFoundException;
import salve.dependency.Locator;
import salve.spring.model.A;
import salve.spring.model.C;
import salve.spring.model.Injected;

public class SpringBeanLocatorTest {
	private static Locator locator;
	private static Object injected;

	private static final String BEAN_NAME = "salve/spring/model/Injected";

	@Test
	public void testByType() {
		Injected i = (Injected) injected;
		Assert.assertNotNull(i.getC());
		Assert.assertEquals(i.getC().getClass(), C.class);
	}

	@Test
	public void testNoneFound() {
		Injected i = (Injected) injected;
		try {
			i.getE();
			Assert.fail("Should have hit an exception");
		} catch (DependencyNotFoundException e) {
			// noop
		}

	}

	@Test
	public void testResolutionConflict() {
		Injected i = (Injected) injected;
		try {
			i.getD();
			Assert.fail("Should have hit an exception");
		} catch (DependencyNotFoundException e) {
			// noop
		}
	}

	@Test
	public void testSpringBeanId() {
		Injected i = (Injected) injected;
		Assert.assertNotNull(i.getA());
		Assert.assertEquals(i.getA().getClass(), A.class);
	}

	@Test
	public void testToString() {
		Assert.assertNotNull(locator.toString());
	}

	@BeforeClass
	public static void init() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:salve/spring/context.xml");
		locator = new SpringBeanLocator(context);

		DependencyLibrary.addLocator(locator);

		ClassLoader classLoader = SpringBeanLocatorTest.class.getClassLoader();
		BytecodePool pool = new BytecodePool();
		pool.addLoader(new ClassLoaderLoader(classLoader));

		byte[] bytecode = pool.loadBytecode(BEAN_NAME);
		if (bytecode == null) {
			throw new RuntimeException("Could not load bytecode for "
					+ BEAN_NAME);
		}

		DependencyInstrumentor inst = new DependencyInstrumentor();
		bytecode = inst.instrument(classLoader, BEAN_NAME, bytecode);
		injected = pool.loadClass(BEAN_NAME, bytecode).newInstance();
	}
}
