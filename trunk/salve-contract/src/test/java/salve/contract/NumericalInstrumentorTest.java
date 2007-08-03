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
package salve.contract;

import org.junit.Test;

import salve.InstrumentationException;

public class NumericalInstrumentorTest extends AbstractContractInstrumentorTest {
	public void foo() {
		double d;
		Double dd = null;

		if (dd > 0) {
			d = 5;
		}
	}

	@Test
	public void testArgumentTypeErrorChecking() throws Exception {
		try {
			create("IllegalArgBean");
			fail();
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				fail();
			}
		}
	}

	@Test
	public void testDoubleReturnTypes() throws Exception {
		Bean bean = (Bean) create("Bean");
		assertTrue(1l == bean.test12(1l));
		assertTrue(new Double(1).equals(bean.test13(1)));

		try {
			bean.test12(0l);
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

		try {
			bean.test13(new Double(0));
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

	}

	@Test
	public void testDoubles() throws Exception {
		Bean bean = (Bean) create("Bean");

		bean.test3(1);
		bean.test3(0);
		try {
			bean.test3(-1);
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}

		bean.test4(new Double(1));
		bean.test4(new Double(0));
		try {
			bean.test4(new Double(-1));
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testFloatReturnTypes() throws Exception {
		Bean bean = (Bean) create("Bean");
		assertTrue(1f == bean.test14(1f));
		assertTrue(new Float(1).equals(bean.test15(1f)));

		try {
			bean.test14(0);
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

		try {
			bean.test15(new Integer(0));
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

	}

	@Test
	public void testFloats() throws Exception {
		Bean bean = (Bean) create("Bean");

		bean.test5(1.0f);
		bean.test5(0);
		try {
			bean.test5(-1.0f);
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}

		bean.test6(new Float(1.0f));
		bean.test6(new Float(0));
		try {
			bean.test6(new Float(-1.0f));
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testGE0Errors() throws Exception {
		Bean bean = (Bean) create("Bean");

		try {
			bean.test9(-1, 0, 1, -1);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testGT0Errors() throws Exception {
		Bean bean = (Bean) create("Bean");

		try {
			bean.test9(-1, 0, 0, 0);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			bean.test9(-1, 0, -1, 0);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testIntegerReturnTypes() throws Exception {
		Bean bean = (Bean) create("Bean");
		assertTrue(1 == bean.test10(1));
		assertTrue(new Integer(1).equals(bean.test10(1)));

		try {
			bean.test10(0);
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

		try {
			bean.test11(new Integer(0));
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

	}

	@Test
	public void testIntegers() throws Exception {
		Bean bean = (Bean) create("Bean");

		bean.test1(1);
		bean.test1(0);
		try {
			bean.test1(-1);
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}

		bean.test2(new Integer(1));
		bean.test2(new Integer(0));
		try {
			bean.test2(new Integer(-1));
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testLE0Errors() throws Exception {
		Bean bean = (Bean) create("Bean");

		try {
			bean.test9(-1, 1, 1, 0);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testLogicNoErrors() throws Exception {
		Bean bean = (Bean) create("Bean");
		bean.test9(-1, 0, 1, 0);
		bean.test9(-1, -1, 1, 1);
	}

	@Test
	public void testLongReturnTypes() throws Exception {
		Bean bean = (Bean) create("Bean");
		assertTrue(1l == bean.test16(1l));
		assertTrue(new Long(1).equals(bean.test16(1l)));

		try {
			bean.test16(0);
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

		try {
			bean.test17(new Long(0));
			fail("Expected IllegalStateException - GT0 violated");
		} catch (IllegalStateException e) {
			// expected
		}

	}

	@Test
	public void testLongs() throws Exception {
		Bean bean = (Bean) create("Bean");

		bean.test7(1l);
		bean.test7(0);
		try {
			bean.test7(-1l);
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}

		bean.test8(new Long(1l));
		bean.test8(new Long(0));
		try {
			bean.test8(new Long(-1l));
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testLT0Errors() throws Exception {
		Bean bean = (Bean) create("Bean");

		try {
			bean.test9(0, 0, 1, 0);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			bean.test9(1, -1, 1, 0);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testReturnTypeErrorChecking() throws Exception {
		try {
			create("IllegalReturnBean");
			fail();
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				fail();
			}
		}
	}

	public static class Bean {
		void test1(@GE0
		int b) {
		}

		@GT0
		int test10(int a) {
			return a;
		}

		@GT0
		Integer test11(int a) {
			return a;
		}

		@GT0
		double test12(double a) {
			return a;
		}

		@GT0
		Double test13(double a) {
			return a;
		}

		@GT0
		float test14(float a) {
			return a;
		}

		@GT0
		Float test15(float a) {
			return a;
		}

		@GT0
		long test16(long a) {
			return a;
		}

		@GT0
		Long test17(long a) {
			return a;
		}

		void test2(@GE0
		Integer b) {
		}

		void test3(@GE0
		double b) {
		}

		void test4(@GE0
		Double b) {
		}

		void test5(@GE0
		float b) {
		}

		void test6(@GE0
		Float b) {
		}

		void test7(@GE0
		long b) {
		}

		void test8(@GE0
		Long b) {
		}

		void test9(@LT0
		int a, @LE0
		int b, @GT0
		int c, @GE0
		int d) {
		}
	}

	class IllegalArgBean {
		void test(@GT0
		Object fii) {
		}
	}

	class IllegalReturnBean {
		@GT0
		Object test() {
			return null;
		}
	}
}