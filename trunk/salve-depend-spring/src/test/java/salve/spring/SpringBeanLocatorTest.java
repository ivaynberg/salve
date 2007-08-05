package salve.spring;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import salve.depend.DependencyInstrumentor;
import salve.depend.DependencyLibrary;
import salve.depend.DependencyNotFoundException;
import salve.depend.Locator;
import salve.loader.BytecodePool;
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

		ClassLoader loader = SpringBeanLocatorTest.class.getClassLoader();
		BytecodePool pool = new BytecodePool().addLoaderFor(loader);

		injected = pool.instrumentIntoClass(BEAN_NAME,
				new DependencyInstrumentor()).newInstance();
	}
}
