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

public class NotEmptyContractInstrumentorTest extends AbstractContractInstrumentorTestSupport {
	public static class Bean {
		public static final String NULL = new String("!!!not-empty!!!");

		public Object test(Object arg1) {
			return arg1;
		}

		@NotEmpty
		public String test(Object arg1, @NotEmpty String arg2, Object arg3) {
			return NULL.equals(arg2) ? null : arg2;
		}

		/**
		 * Issue 2 test: NotEmpty does not work on a String method param right
		 * before a var-args param
		 */
		public void testParamBeforeVarArgs(String param1, @NotEmpty String param2, String... params) {
		}

	}

	public static class NonStringParameterBean {
		public void testEmptyNull(@NotEmpty int a) {

		}
	}

	public static class VoidReturnBean {
		@NotEmpty
		public void test() {
		}
	}

	@Test
	public void testArgumentTypeErrorChecking() throws Exception {
		try {
			create("NonStringParameterBean");
			fail("Expected error instrumenting non-string notempty argument");
		} catch (InstrumentationException e) {

		}
	}

	@Test
	public void testNotEmpty() throws Exception {
		Bean bean = (Bean) create("Bean");
		final String token = new String("foo");

		assertEquals(token, bean.test(token));
		assertEquals(token, bean.test(null, token, null));

		try {
			bean.test(null, null, null);
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			bean.test(null, "  ", null);
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
		}

		try {
			bean.test(null, Bean.NULL, null);
			fail("Expected " + IllegalStateException.class.getName());
		} catch (IllegalStateException e) {
			// expected
			// System.out.println(e.getMessage());
		}

		// test signatures with varargs

		try {
			bean.testParamBeforeVarArgs(null, "  ", "vararg0", "vararg1");
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
		}
		bean.testParamBeforeVarArgs(null, "not empty", "vararg0", "vararg1");

	}

	@Test
	public void testReturnTypeErrorChecking() throws Exception {

		try {
			create("VoidReturnBean");
			fail("Expected error instrumenting notempty method with non-string return type");
		} catch (InstrumentationException e) {

		}

	}

}
