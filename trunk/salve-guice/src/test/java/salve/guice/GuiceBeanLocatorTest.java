package salve.guice;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import salve.dependency.DependencyLibrary;
import salve.dependency.Locator;
import salve.dependency.impl.PojoInstrumentor;
import salve.guice.model.Blue;
import salve.guice.model.Injected;
import salve.guice.model.MockService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class GuiceBeanLocatorTest {
	private static Injector injector;
	private static Object injected;
	private static Locator locator;

	@Test
	public void testLookupByType() {
		MockService ts = ((Injected) injected).getTestService();
		Assert.assertNotNull(ts);
		Assert.assertEquals(ts.getName(), ts.getClass().getName());
	}

	@Test
	public void testLookupByTypeAndAnnot() {
		MockService ts = ((Injected) injected).getBlueTestService();
		Assert.assertNotNull(ts);
		Assert.assertEquals(ts.getName(), "BlueTestService");
	}

	@Test
	public void testToString() {
		Assert.assertNotNull(locator.toString());
	}

	@BeforeClass
	public static void init() throws Exception {
		Module module = new AbstractModule() {

			@Override
			protected void configure() {
				bind(MockService.class).in(Scopes.SINGLETON);

				Key<MockService> blueKey = Key.get(MockService.class,
						Blue.class);
				bind(blueKey).toInstance(new MockService("BlueTestService"));

			}

		};

		injector = Guice.createInjector(module);
		locator = new GuiceBeanLocator(injector);
		DependencyLibrary.clear();

		DependencyLibrary.addLocator(locator);

		ClassPool cp = new ClassPool(ClassPool.getDefault());
		cp.appendClassPath(new ClassClassPath(GuiceBeanLocatorTest.class));
		CtClass clazz = cp.get("salve.guice.model.Injected");
		PojoInstrumentor inst = new PojoInstrumentor(clazz);
		inst.instrument();
		injected = inst.getInstrumented().toClass().newInstance();
	}
}
