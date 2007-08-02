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

public class NumericalInstrumentorTest extends AbstractContractInstrumentorTest {
	@Test
	public void testArgumentTypeErrorChecking() throws Exception {
	}

	@Test
	public void testReturnTypeErrorChecking() throws Exception {
	}

	@Test
	public void testTypeHandling() throws Exception {
		Bean bean = (Bean) create("Bean");

		bean.test1(-1, 1, -1);
		bean.test1(-1, 0, -1);
		try {
			bean.test1(-1, -1, -1);
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}

		Integer i1 = new Integer(1);
		Integer iz = new Integer(0);
		Integer in1 = new Integer(-1);

		bean.test2(in1, i1, in1);
		bean.test2(in1, iz, in1);
		try {
			bean.test2(in1, in1, in1);
			fail("Expected IllegalArgumentException - GE0 violated");
		} catch (IllegalArgumentException e) {
			// expected
		}

	}

	public static class Bean {
		void test1(int a, @GE0
		int b, int c) {
		}

		void test2(Integer a, @GE0
		Integer b, Integer c) {
		}

	}

}
