package salve.dependency;

import java.lang.annotation.Annotation;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import salve.asm.loader.BytecodePool;
import salve.asm.loader.ClassLoaderLoader;
import salve.dependency.impl.DependencyConstants;

public class DependencyInstrumentorTest {
	private static String BEAN_NAME = "salve/dependency/Bean";
	private static Class<?> beanClass;

	private static BlueDependency blue;
	private static RedDependency red;
	private static Locator locator;

	@Before
	public void resetMocks() {
		EasyMock.reset(blue, red, locator);
	}

	@Test
	public void testAnnotations() throws Exception {
		Annotation[] annots = beanClass.getDeclaredField(
				DependencyConstants.KEY_FIELD_PREFIX + "red").getAnnotations();
		Assert.assertEquals(2, annots.length);
		final Class a1 = annots[0].annotationType();
		final Class a2 = annots[1].annotationType();
		Assert.assertTrue(a1.equals(Square.class) && a2.equals(Circle.class)
				|| a2.equals(Square.class) && a1.equals(Circle.class));

		annots = beanClass.getDeclaredField("blue").getAnnotations();
		Assert.assertEquals(0, annots.length);

		annots = beanClass.getDeclaredField("black").getAnnotations();
		Assert.assertEquals(1, annots.length);
		Assert.assertEquals(Circle.class, annots[0].annotationType());

	}

	@Test
	public void testFieldRead() throws Exception {
		Bean bean = (Bean) beanClass.newInstance();

		/*
		 * when we call bean.method1() both red and blue will be looked up. when
		 * we call bean.method2() only red will be looked up. blue needs to be
		 * looked up only once because it is cached in the field. red is looked
		 * up twice because it is cached per method and we call two methods
		 */
		EasyMock.expect(
				locator.locate(new KeyImpl(RedDependency.class, Bean.class,
						DependencyConstants.KEY_FIELD_PREFIX + "red")))
				.andReturn(red).times(2);
		EasyMock.expect(locator.locate(new KeyImpl(BlueDependency.class)))
				.andReturn(blue);
		// inside bean.method1() and bean.method2() we call all four methodsare
		blue.method1();
		blue.method1();
		blue.method2();
		blue.method2();
		red.method1();
		red.method1();
		red.method2();
		red.method2();

		EasyMock.replay(locator, blue, red);
		bean.method1();
		bean.method2();
		EasyMock.verify(locator, blue, red);
	}

	@Test
	public void testFieldReadOnReturn() throws Exception {
		Bean bean = (Bean) beanClass.newInstance();
		EasyMock.expect(
				locator.locate(new KeyImpl(RedDependency.class, Bean.class,
						DependencyConstants.KEY_FIELD_PREFIX + "red")))
				.andReturn(red);
		EasyMock.expect(locator.locate(new KeyImpl(BlueDependency.class)))
				.andReturn(blue);

		EasyMock.replay(locator, blue, red);
		Assert.assertTrue(bean.getRed() == red);
		Assert.assertTrue(bean.getBlue() == blue);
		EasyMock.verify(locator, blue, red);
	}

	@Test
	public void testFieldRemoval() throws Exception {
		try {
			beanClass.getDeclaredField("red");
			Assert.fail("Field `red` should have been removed");
		} catch (NoSuchFieldException e) {
			// noop
		}
	}

	@Test
	public void testFieldWrite() throws Exception {
		Bean bean = (Bean) beanClass.newInstance();

		try {
			bean.setBlue(blue);
			Assert
					.fail("Attempted to write to removed dependency field and did not get IllegalFieldWriteException");
		} catch (IllegalFieldWriteException e) {
			// noop
		}
		try {
			bean.setRed(red);
			Assert
					.fail("Attempted to write to removed dependency field and did not get IllegalFieldWriteException");
		} catch (IllegalFieldWriteException e) {
			// noop
		}

		// make sure non dependency fields are left alone
		BlackDependency black = EasyMock.createMock(BlackDependency.class);
		Assert.assertTrue(bean.getBlack() == null);
		bean.setBlack(black);
		Assert.assertTrue(bean.getBlack() == black);

		Assert.assertTrue(Bean.getStaticBlack() == null);
		Bean.setStaticBlack(black);
		Assert.assertTrue(Bean.getStaticBlack() == black);

	}

	@BeforeClass
	public static void initClass() throws Exception {
		loadBeans();
		initDependencyLibrary();
	}

	private static void initDependencyLibrary() {
		DependencyLibrary.clear();

		blue = EasyMock.createMock(BlueDependency.class);
		red = EasyMock.createMock(RedDependency.class);
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

		DependencyInstrumentor inst = new DependencyInstrumentor();
		bytecode = inst.instrument(classLoader, BEAN_NAME, bytecode);
		beanClass = pool.loadClass(BEAN_NAME, bytecode);
	}
}