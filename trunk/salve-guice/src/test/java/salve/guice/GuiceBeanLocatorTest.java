package salve.guice;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import salve.asm.loader.BytecodePool;
import salve.asm.loader.ClassLoaderLoader;
import salve.dependency.DependencyInstrumentor;
import salve.dependency.DependencyLibrary;
import salve.dependency.Locator;
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

	private static final String BEAN_NAME = "salve/guice/model/Injected";

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

		ClassLoader classLoader = GuiceBeanLocatorTest.class.getClassLoader();
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
