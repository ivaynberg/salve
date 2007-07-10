package salve.spring;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import salve.dependency.DependencyLibrary;
import salve.dependency.DependencyNotFoundException;
import salve.dependency.Locator;
import salve.dependency.impl.PojoInstrumentor;
import salve.spring.model.A;
import salve.spring.model.B;
import salve.spring.model.C;
import salve.spring.model.Injected;

public class SpringBeanLocatorTest {
	private static Locator locator;
	private static Object injected;

	@Test
	public void testByType() {
		Injected i = (Injected) injected;
		Assert.assertNotNull(i.getC());
		Assert.assertEquals(i.getC().getClass(), C.class);
	}

	@Test
	public void testDefaultToFieldName() {
		Injected i = (Injected) injected;
		Assert.assertNotNull(i.getB());
		Assert.assertEquals(i.getB().getClass(), B.class);
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

		ClassPool cp = new ClassPool(ClassPool.getDefault());
		cp.appendClassPath(new ClassClassPath(SpringBeanLocatorTest.class));
		CtClass clazz = cp.get("salve.spring.model.Injected");
		PojoInstrumentor inst = new PojoInstrumentor(clazz);
		inst.instrument();
		CtClass instrumented = inst.getInstrumented();
		injected = instrumented.toClass().newInstance();
	}
}
