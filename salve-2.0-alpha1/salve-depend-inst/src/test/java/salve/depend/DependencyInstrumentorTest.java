/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.depend;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import salve.depend.cache.NoopCacheProvider;
import salve.loader.ClassLoaderLoader;
import salve.loader.TestBytecodePool;
import salve.util.EasyMockTemplate;

public class DependencyInstrumentorTest extends Assert implements Constants {
	private static Class<?> beanClass;

	private static BlueDependency blue;
	private static RedDependency red;
	private static Locator locator;

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
		DependencyLibrary.setCacheProvider(new NoopCacheProvider());
	}

	private static void loadBeans() throws Exception {
		final DependencyInstrumentor inst = new DependencyInstrumentor();
		ClassLoader classLoader = DependencyInstrumentorTest.class.getClassLoader();
		TestBytecodePool pool = new TestBytecodePool(new ClassLoaderLoader(classLoader));
		beanClass = pool.instrumentIntoClass("salve/depend/Bean", inst);
		// instrument inner and anonymous classes
		pool.instrumentIntoClass("salve/depend/Bean$InnerBean", inst);
		pool.instrumentIntoClass("salve/depend/Bean$1", inst);
	}

	@Before
	public void resetMocks() {
		EasyMock.reset(blue, red, locator);
	}

	@Test
	public void testAnnotations() throws Exception {
		Annotation[] annots = beanClass.getDeclaredField(REMOVED_FIELD_PREFIX + "red").getAnnotations();
		assertNotNull(annots);

		List<Class<?>> types = new ArrayList<Class<?>>(annots.length);
		for (final Annotation annot : annots) {
			types.add(annot.annotationType());
		}

		assertEquals(3, types.size());
		assertTrue(types.contains(Square.class));
		assertTrue(types.contains(Circle.class));
		assertTrue(types.contains(DependencyFieldInfo.class));

		annots = beanClass.getDeclaredField("blue").getAnnotations();
		assertNotNull(annots);
		assertEquals(0, annots.length);

		annots = beanClass.getDeclaredField("black").getAnnotations();
		assertNotNull(annots);
		assertEquals(1, annots.length);
		assertEquals(annots[0].annotationType(), Circle.class);
	}

	@Test
	public void testAnonClassFieldRead() throws Exception {
		new EasyMockTemplate(locator, red) {

			@Override
			protected void setupExpectations() {
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red"))).andReturn(red);
				red.method1();
			}

			@Override
			protected void testExpectations() throws Exception {

				Bean bean = (Bean) beanClass.newInstance();
				bean.methodAnonymous();
			}
		}.test();
	}

	@SuppressWarnings("static-access")
	@Test
	public void testClinitMerge() throws Exception {
		Bean bean = (Bean) beanClass.newInstance();
		assertTrue(Bean.FORCE_CLINIT != 0);
	}

	@Test
	public void testDoubleInstrumentation() throws Exception {
		final DependencyInstrumentor inst = new DependencyInstrumentor();
		ClassLoader classLoader = DependencyInstrumentorTest.class.getClassLoader();
		TestBytecodePool pool = new TestBytecodePool(new ClassLoaderLoader(classLoader));
		pool.instrumentIntoBytecode("salve/depend/Bean2", inst);
		pool.instrumentIntoBytecode("salve/depend/Bean2", inst);
		pool.loadClass("salve/depend/Bean2");
	}

	@Test
	public void testFieldAccessInConstructor() throws Exception {

		new EasyMockTemplate(locator) {

			@Override
			protected void setupExpectations() {
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red"))).andReturn(red);
				EasyMock.expect(locator.locate(new TestKey(BlueDependency.class))).andReturn(blue);
			}

			@Override
			protected void testExpectations() throws Exception {
				Constructor<?> c = beanClass.getConstructor(int.class);
				c.newInstance(5);
			}
		}.test();
	}

	@Test
	public void testFieldRead() throws Exception {

		new EasyMockTemplate(locator, red, blue) {

			@Override
			protected void setupExpectations() {
				/*
				 * when we call bean.method1() both red and blue will be looked
				 * up. when we call bean.method2() only red will be looked up.
				 * blue needs to be looked up only once because it is cached in
				 * the field. red is looked up twice because it is cached per
				 * method and we call two methods
				 */
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red"))).andReturn(red)
						.times(2);
				EasyMock.expect(locator.locate(new TestKey(BlueDependency.class))).andReturn(blue);
				// inside bean.method1() and bean.method2() we call all four
				// methods
				blue.method1();
				blue.method1();
				blue.method2();
				blue.method2();
				red.method1();
				red.method1();
				EasyMock.expect(red.method2()).andReturn(null);
				EasyMock.expect(red.method2()).andReturn(null);

				// Bean.getStaticBlue().method1();
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "staticBlue")))
						.andReturn(blue);
				blue.method1();
			}

			@Override
			protected void testExpectations() throws Exception {
				Bean bean = (Bean) beanClass.newInstance();
				bean.method1();
				bean.method2();
				Bean.getStaticBlue().method1();
			}
		}.test();
	}

	@Test
	public void testFieldReadOnReturn() throws Exception {

		new EasyMockTemplate(locator, red, blue) {

			@Override
			protected void setupExpectations() {
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red"))).andReturn(red);
				EasyMock.expect(locator.locate(new TestKey(BlueDependency.class))).andReturn(blue);
			}

			@Override
			protected void testExpectations() throws Exception {
				Bean bean = (Bean) beanClass.newInstance();
				assertTrue(bean.getRed() == red);
				assertTrue(bean.getBlue() == blue);
			}
		}.test();
	}

	@Test
	public void testFieldRemoval() throws Exception {
		try {
			beanClass.getDeclaredField("red");
			fail("Field `red` should have been removed");
		} catch (NoSuchFieldException e) {
			// noop
		}
	}

	@Test
	public void testFieldWrite() throws Exception {
		final Bean bean = (Bean) beanClass.newInstance();

		try {
			bean.setBlue(blue);
			Assert.fail("Attempted to write to removed dependency field and did not get IllegalFieldWriteException");
		} catch (IllegalFieldWriteException e) {
			// noop
		}
		try {
			bean.setRed(red);
			Assert.fail("Attempted to write to removed dependency field and did not get IllegalFieldWriteException");
		} catch (IllegalFieldWriteException e) {
			// noop
		}

		// make sure non dependency fields are left alone
		BlackDependency black = EasyMock.createMock(BlackDependency.class);
		assertTrue(bean.getBlack() == null);
		bean.setBlack(black);
		assertTrue(bean.getBlack() == black);
	}

	@Test
	public void testInnerClassFieldRead() throws Exception {
		new EasyMockTemplate(locator, red, blue) {

			@Override
			protected void setupExpectations() {
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "red"))).andReturn(red);
				EasyMock.expect(locator.locate(new TestKey(BlueDependency.class))).andReturn(blue);

				blue.method1();
				red.method1();
			}

			@Override
			protected void testExpectations() throws Exception {
				Bean bean = (Bean) beanClass.newInstance();
				bean.methodInner();
			}
		}.test();
	}

	@Test
	public void testInterfaceInstrumentation() throws Exception {
		ClassLoader classLoader = DependencyInstrumentorTest.class.getClassLoader();
		TestBytecodePool pool = new TestBytecodePool(new ClassLoaderLoader(classLoader));

		@SuppressWarnings("unused")
		Class<?> interfaceClass = pool.instrumentIntoClass("salve/depend/Interface", new DependencyInstrumentor());
	}

	@Test
	public void testStaticFieldRead() throws Exception {

		new EasyMockTemplate(locator, red, blue) {

			@Override
			protected void setupExpectations() {
				EasyMock.expect(locator.locate(new FieldKey(Bean.class, REMOVED_FIELD_PREFIX + "staticBlue")))
						.andReturn(blue);
				blue.method1();
			}

			@Override
			protected void testExpectations() throws Exception {
				Bean bean = (Bean) beanClass.newInstance();
				Bean.getStaticBlue().method1();
			}
		}.test();
	}

	@Test
	public void testStaticFieldWrite() throws Exception {
		final Bean bean = (Bean) beanClass.newInstance();

		try {
			Bean.setStaticBlue(blue);
			Assert.fail("Attempted to write to removed dependency field and did not get IllegalFieldWriteException");
		} catch (IllegalFieldWriteException e) {
			// noop
		}

		// make sure non dependency fields are left alone
		BlackDependency black = EasyMock.createMock(BlackDependency.class);

		assertTrue(Bean.getStaticBlack() == null);
		Bean.setStaticBlack(black);
		assertTrue(Bean.getStaticBlack() == black);
	}
}
