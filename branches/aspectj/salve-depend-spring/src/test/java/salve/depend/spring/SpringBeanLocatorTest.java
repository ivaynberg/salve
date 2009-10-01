package salve.depend.spring;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import salve.depend.DependencyLibrary;
import salve.depend.DependencyNotFoundException;
import salve.depend.DependencyResolutionConflictException;
import salve.depend.FieldKey;
import salve.depend.Locator;
import salve.depend.spring.model.A;
import salve.depend.spring.model.C;
import salve.depend.spring.model.Injected;

public class SpringBeanLocatorTest {
	private static Locator locator;
	private static Object injected;

	private static final String BEAN_NAME = "salve/depend/spring/model/Injected";

	@Test
	public void testByType() {
		Assert.assertNotNull(getFieldValue("c"));
		Assert.assertEquals(getFieldValue("c").getClass(), C.class);
	}

	private static Object getFieldValue(String field) {
		return DependencyLibrary.locate(new FieldKey(Injected.class, field));
	}

	@Test
	public void testNoneFound() {
		try {
			getFieldValue("e");
			Assert.fail("Should have hit an exception");
		} catch (DependencyNotFoundException e) {
			// noop
		}

	}

	@Test
	public void testResolutionConflict() {
		try {
			getFieldValue("d");
			Assert.fail("Should have hit an exception");
		} catch (DependencyResolutionConflictException e) {
			// noop
		}
	}

	@Test
	public void testSpringBeanId() {

		Assert.assertNotNull(getFieldValue("a"));
		Assert.assertEquals(getFieldValue("a").getClass(), A.class);
	}

	@Test
	public void testToString() {
		Assert.assertNotNull(locator.toString());
	}

	@BeforeClass
	public static void init() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:salve/depend/spring/context.xml");
		locator = new SpringBeanLocator(context);

		DependencyLibrary.addLocator(locator);
	}
}
