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

public class NotNullContractInstrumentorTest extends
		AbstractContractInstrumentorTest {
	@Test
	public void testArgumentTypeErrorChecking() throws Exception {
		try {
			create("NotNullPrimitiveArgumentBean");
			fail("Expected error instrumenting primitive notnull argument");
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				throw e;
			}

		}
	}

	@Test
	public void testNotNull() throws Exception {
		NotNullBean bean = (NotNullBean) create("NotNullBean");
		final Object token = new Object();

		assertTrue(token == bean.testNotAnnotated(token));
		assertTrue(token == bean.testNotNull(null, token, null));

		try {
			bean.testNotNull(null, null, null);
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
			// System.out.println(e.getMessage());
		}

		try {
			bean.testNotNull(null, NotNullBean.NULL, null);
			fail("Expected " + IllegalStateException.class.getName());
		} catch (IllegalStateException e) {
			// expected
			// System.out.println(e.getMessage());
		}

	}

	@Test
	public void testReturnTypeErrorChecking() throws Exception {
		try {
			create("NotNullPrimitiveReturnBean");
			fail("Expected error instrumenting notnull method with primitive return type");
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				throw e;
			}

		}

		try {
			create("NotNullVoidReturnBean");
			fail("Expected error instrumenting notnull method with void return type");
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				throw e;
			}

		}

	}

	public static class NotNullBean {
		public static final Object NULL = new Object();

		public Object testNotAnnotated(Object arg1) {
			return arg1;
		}

		@NotNull
		public Object testNotNull(Object arg1, @NotNull
		Object arg2, Object arg3) {
			return arg2 == NULL ? null : arg2;
		}
	}

	public static class NotNullPrimitiveArgumentBean {
		public void testNotNull(@NotNull
		int a) {
		}
	}

	public static class NotNullPrimitiveReturnBean {
		@NotNull
		public int testNotNull() {
			return 0;
		}
	}

	public static class NotNullVoidReturnBean {
		@NotNull
		public void testNotNull() {
		}
	}

}
