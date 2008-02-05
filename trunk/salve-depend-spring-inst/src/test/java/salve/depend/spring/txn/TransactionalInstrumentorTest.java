/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package salve.depend.spring.txn;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

import salve.depend.DependencyLibrary;
import salve.depend.Locator;
import salve.depend.cache.NoopCacheProvider;
import salve.loader.BytecodePool;
import salve.util.EasyMockTemplate;

public class TransactionalInstrumentorTest extends Assert {
	private static String METHODBEAN_NAME = "salve/depend/spring/txn/MethodBean";
	private static String CLASSBEAN_NAME = "salve/depend/spring/txn/ClassBean";
	private static Class<?> mbClass;
	private static Class<?> cbClass;

	private static Locator locator;
	private static TransactionManager tm;

	@Before
	public void initMocks() {
		EasyMock.reset(tm, locator);

	}

	@Test
	public void testArgs() throws Exception {
		final MethodBean bean = (MethodBean) mbClass.newInstance();
		new CommitTemplate() {
			@Override
			protected void testExpectations() throws Exception {

				Object[] array1 = new Object[0];
				assertTrue(bean.args2(1, array1, null) == array1);

			}
		}.test();
		new CommitTemplate() {
			@Override
			protected void testExpectations() throws Exception {

				double[] array2 = new double[0];
				assertTrue(bean.args2(2, null, array2) == array2);

			}
		}.test();

	}

	@SuppressWarnings("static-access")
	@Test
	public void testClinit() throws Exception {
		MethodBean bean = (MethodBean) mbClass.newInstance();
		assertTrue(bean.CLINIT_FORCER != 0);
	}

	@Test
	public void testReturnValue() throws Exception {
		new CommitTemplate() {
			@Override
			protected void testExpectations() throws Exception {
				MethodBean bean = (MethodBean) mbClass.newInstance();
				Object token = new Object();
				Object token2 = bean.ret(token);
				assertTrue(token == token2);
			}
		}.test();
	}

	@Test
	public void testSimple() throws Exception {

		new CommitTemplate() {

			@Override
			protected void testExpectations() throws Exception {
				((MethodBean) mbClass.newInstance()).simple();
			}

		}.test();
	}

	@Test
	public void testThrowsChecked() throws Exception {

		new RollbackTemplate() {

			@Override
			protected void testExpectations() throws Exception {
				MethodBean bean = (MethodBean) mbClass.newInstance();
				try {
					bean.exception(1, null);
					fail("Expected exception to be thrown");
				} catch (IndexOutOfBoundsException e) {

				}
			}
		}.test();

	}

	/**
	 * Test that <code>throw</code> clauses in code do not cause a rollback
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThrowsNormalReturn() throws Exception {

		new CommitTemplate() {

			@Override
			protected void testExpectations() throws Exception {
				MethodBean bean = (MethodBean) mbClass.newInstance();
				Object token = new Object();
				@SuppressWarnings("unused")
				Object token2 = bean.exception(0, token);
			}

		}.test();
	}

	/**
	 * Test that an unexpected run time exception causes a rollback
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThrowsUnchecked() throws Exception {

		new RollbackTemplate() {

			@Override
			protected void testExpectations() throws Exception {
				MethodBean bean = (MethodBean) mbClass.newInstance();
				try {
					bean.exception(2, null);
					fail("Expected exception to be thrown");
				} catch (IllegalStateException e) {
					// expected
				}
			}

		}.test();
	}

	/**
	 * Test that constructors of an annotated class are not instrumented but
	 * methods are
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	// @Test
	public void testTransactionalClassInstrumentation() throws Exception {
		// FIXME reenable!
		// test constructor is NOT instrumented

		// EasyMock.replay(locator, ptm);
		final ClassBean bean = (ClassBean) cbClass.newInstance();
		assertTrue(bean.CLINIT_FORCER != 0);
		// EasyMock.verify(locator, ptm);

		// test method is instrumented

		new CommitTemplate() {

			@Override
			protected void testExpectations() throws Exception {
				bean.method();
			}

		}.test();

	}

	/**
	 * One time initialization code
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void initClass() throws Exception {
		loadBeans();
		initDependencyLibrary();
	}

	/**
	 * Installs mocks into dependency library
	 */
	private static void initDependencyLibrary() {
		DependencyLibrary.clear();

		tm = EasyMock.createMock(TransactionManager.class);
		locator = EasyMock.createMock(Locator.class);
		DependencyLibrary.addLocator(locator);
		DependencyLibrary.setCacheProvider(new NoopCacheProvider());
	}

	/**
	 * Loads and instruments bytecode for test beans
	 * 
	 * @throws Exception
	 */
	private static void loadBeans() throws Exception {
		ClassLoader loader = TransactionalInstrumentorTest.class
				.getClassLoader();
		BytecodePool pool = new BytecodePool().addLoaderFor(loader);
		TransactionalInstrumentor inst = new TransactionalInstrumentor();

		mbClass = pool.instrumentIntoClass(METHODBEAN_NAME, inst);
		cbClass = pool.instrumentIntoClass(CLASSBEAN_NAME, inst);
	}

	/**
	 * Test that a mere <code>throws</code> declaration does not cause a
	 * rollback
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExceptionOk() throws Exception {
		new CommitTemplate() {
			@Override
			protected void testExpectations() throws Exception {
				((MethodBean) mbClass.newInstance()).exceptionOk();
			}
		}.test();
	}

	/**
	 * Test that expected exception still properly calls finish(ex,status)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExceptionOkWithException() throws Exception {
		new RollbackTemplate() {
			@Override
			protected void testExpectations() throws Exception {
				try {
					((MethodBean) mbClass.newInstance())
							.exceptionOkWithException();
				} catch (NoRollbackException e) {
					// noop
				}
			}
		}.test();
	}

	/**
	 * Test that unexpected exception thrown from within transactional method
	 * causes a rollback
	 * 
	 * @throws Exception
	 */
	@Test
	public void testException() throws Exception {
		new RollbackTemplate() {
			@Override
			protected void testExpectations() throws Exception {
				try {
					((MethodBean) mbClass.newInstance()).exception();
				} catch (RollbackException e) {
					// noop
				}

			}
		}.test();
	}

	/**
	 * Test that unexpected exception thrown from a method call inside a
	 * transactional method causes the transactional method to rollback
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExceptionFromWithin() throws Exception {
		new RollbackTemplate() {

			@Override
			protected void testExpectations() throws Exception {
				try {
					((MethodBean) mbClass.newInstance()).exceptionFromWithin();
					fail("exception expected");
				} catch (RuntimeException e) {
					// noop
				}

			}

		}.test();
	}

	/**
	 * Easy mock template that sets up expections for a single failed runthrough
	 * of a transactional method
	 * 
	 * @author igor.vaynberg
	 */
	private static abstract class RollbackTemplate extends TxnTemplate {

		@Override
		protected void setupExpectations(Object status) {
			super.setupExpectations(status);
			// manager.start(?,?);
			EasyMock.expect(
					tm.start((TransactionAttribute) EasyMock.anyObject(),
							(String) EasyMock.anyObject())).andReturn(status);

			// manager.finish(ex, status);
			tm.finish((Throwable) EasyMock.anyObject(), EasyMock.eq(status));

			// manager.cleanup(status);
			tm.cleanup(status);
		}
	}

	/**
	 * Easy mock template that sets up expections for a single successful
	 * runthrough of a transactional method
	 * 
	 * @author igor.vaynberg
	 */
	private static abstract class CommitTemplate extends TxnTemplate {

		@Override
		protected void setupExpectations(Object status) {
			super.setupExpectations(status);

			// manager.start(?,?);
			EasyMock.expect(
					tm.start((TransactionAttribute) EasyMock.anyObject(),
							(String) EasyMock.anyObject())).andReturn(status);

			// manager.finish(status);
			tm.finish(status);

			// manager.cleanup(status);
			tm.cleanup(status);

		}
	}

	/**
	 * Easy mock template that sets up expections for a single runthrough of a
	 * transactional method
	 * 
	 * @author igor.vaynberg
	 */
	private static abstract class TxnTemplate extends EasyMockTemplate {

		/** status mock */
		private final Object status = new Object();

		public TxnTemplate() {
			super(locator, tm);
		}

		@Override
		protected final void setupExpectations() {
			setupExpectations(status);
		}

		protected void setupExpectations(Object status) {
			// locate transaction manager
			EasyMock.expect(locator.locate(TransactionManager.KEY))
					.andReturn(tm);
		}

	}
}
