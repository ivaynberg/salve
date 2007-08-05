package salve.guice;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import salve.depend.DependencyInstrumentor;
import salve.depend.DependencyLibrary;
import salve.depend.Locator;
import salve.guice.model.Blue;
import salve.guice.model.Injected;
import salve.guice.model.MockService;
import salve.loader.BytecodePool;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class GuiceBeanLocatorTest {
	private static final String BEAN_NAME = "salve/guice/model/Injected";
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

		ClassLoader loader = GuiceBeanLocatorTest.class.getClassLoader();
		BytecodePool pool = new BytecodePool().addLoaderFor(loader);
		Class<?> clazz = pool.instrumentIntoClass(BEAN_NAME,
				new DependencyInstrumentor());
		injected = clazz.newInstance();
	}
}
