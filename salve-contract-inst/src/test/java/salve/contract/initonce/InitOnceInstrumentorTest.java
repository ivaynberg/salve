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
package salve.contract.initonce;

import org.junit.Test;

import salve.contract.AbstractContractInstrumentorTestSupport;
import salve.contract.InitOnce;

public class InitOnceInstrumentorTest extends AbstractContractInstrumentorTestSupport {

	public static class Bean {
		private String foo;

		@InitOnce
		private String once;

		public String getFoo() {
			return foo;
		}

		public String getOnce() {
			return once;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

		public void setOnce(String once) {
			this.once = once;
		}

	}

	@Test
	public void testCannotResetToNull() throws Exception {
		Bean bean = (Bean) create("Bean");
		bean.setOnce("one");
		try {
			bean.setOnce(null);
			fail("Cannot reset previously set value to null, expected " + IllegalArgumentException.class.getName());
		} catch (IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void testCannotSetOnceSet() throws Exception {
		Bean bean = (Bean) create("Bean");

		bean.setOnce("one");
		assertEquals("one", bean.getOnce());

		try {
			bean.setOnce("two");
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalStateException e) {
			// expected

			assertEquals("one", bean.getOnce());
		}
	}

	@Test
	public void testCanSetToNullMultipleTimesBeforeInitialized() throws Exception {
		Bean bean = (Bean) create("Bean");
		bean.setOnce(null);
		bean.setOnce(null);
		bean.setOnce("one");
		try {
			bean.setOnce(null);
			fail("Cannot reset previously set value to null, expected " + IllegalArgumentException.class.getName());
		} catch (IllegalStateException e) {
			// expected
		}
	}

}
