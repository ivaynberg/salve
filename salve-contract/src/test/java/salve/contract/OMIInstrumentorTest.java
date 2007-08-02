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

public class OMIInstrumentorTest extends AbstractContractInstrumentorTest {

	@Test
	public void testProperImplementation() throws Exception {
		final Bean bean = (Bean) create("Bean");
		final Object token = new Object();
		assertTrue(token == bean.test1(token));
		assertTrue(token == bean.test2(token));
		try {
			bean.test3(token);
			fail("Expecting illegal argument exception because super was not called");
		} catch (IllegalStateException e) {

		}
		// test omi annots are inherited from interfaces
		try {
			bean.test4(token);
			fail("Expecting illegal argument exception because super was not called");
		} catch (IllegalStateException e) {

		}
	}

	public static class BaseBean {
		public Object test1(Object o) {
			return o;
		}

		@OverrideMustInvoke
		public Object test2(Object o) {
			return o;
		}

		@OverrideMustInvoke
		public Object test3(Object o) {
			return o;
		}
	}

	public static interface BaseInterface {
		@OverrideMustInvoke
		public Object test4(Object o);
	}

	public static class Bean extends BaseBean implements BaseInterface {
		@Override
		public Object test1(Object o) {
			return o;
		}

		@Override
		public Object test2(Object o) {
			return super.test2(o);
		}

		@Override
		public Object test3(Object o) {
			return o;
		}

		public Object test4(Object o) {
			return null;
		}
	}

}
