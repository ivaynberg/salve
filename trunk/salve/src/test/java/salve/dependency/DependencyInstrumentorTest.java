package salve.dependency;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import salve.asm.loader.BytecodePool;
import salve.asm.loader.ClassLoaderLoader;

public class DependencyInstrumentorTest {
	private static String BEAN_NAME = "salve/dependency/Bean";
	private static Object bean;

	private static BlueDependency blue;
	private static RedDependency red;

	@BeforeClass
	public static void initClass() throws Exception {
		loadBeans();
		initDependencyLibrary();
	}

	@BeforeClass
	private static void initDependencyLibrary() {
		blue = EasyMock.createMock(BlueDependency.class);
		red = EasyMock.createMock(RedDependency.class);
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

		DependencyInstrumentor inst = new DependencyInstrumentor();
		bytecode = inst.instrument(classLoader, BEAN_NAME, bytecode);
		bean = pool.loadClass(BEAN_NAME, bytecode).newInstance();
	}

	@Before
	public void resetMocks() {
		EasyMock.reset(blue, red);
	}

	@Test
	public void test() throws Exception {
		Bean bean = (Bean) DependencyInstrumentorTest.bean;

	}
}
