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
package salve.contract.pe;

import org.junit.Test;

import salve.InstrumentationException;
import salve.contract.AbstractContractInstrumentorTestSupport;
import salve.contract.PE;

public class PEContractInstrumentorTest extends AbstractContractInstrumentorTestSupport {

	public static class TestField__Primitive {
		public void testCode() {
			new PE(Person.class, "address.city.id", "r");
		}
	}

	public static class TestGetter__Primitive {
		public void testCode() {
			new PE(Person.class, "address.city.idInteger", "r");
		}
	}

	public static class TestGettersCode1 {
		public void testCode() {
			new PE(Person.class, "address.city.name", "r");
		}
	}

	public static class TestGettersCode2 {
		public void testCode() {
			new PE(City.class, "name", "r");
		}
	}

	public static class TestGettersCodeBad1 {
		public void testCode() {
			new PE(Person.class, "address2.city.name", "r");
		}
	}

	public static class TestGettersCodeBad2 {
		public void testCode() {
			new PE(Person.class, "address.city2.name", "r");
		}
	}

	public static class TestGettersCodeBad3 {
		public void testCode() {
			new PE(Person.class, "address.city.name2", "r");
		}
	}

	public static class TestGettersCodeBad4 {
		public void testCode() {
			new PE(Person.class, "address.city.name.foo", "r");
		}
	}

	public static class TestGettersCodeBad5 {
		public void testCode() {
			new PeContainer(null, new PE(Person.class, "address2.city.name", "r"));
		}
	}

	@Test
	public void testFields() throws Exception {
		create("TestField__Primitive");
	}

	@Test
	public void testGetters() throws Exception {
		create("TestGettersCode1");
		create("TestGettersCode2");
		create("TestGetter__Primitive");
		try {
			create("TestGettersCodeBad1");
			fail("Should have failed...");
		} catch (InstrumentationException e) {
			// ignore
		}
		try {
			create("TestGettersCodeBad2");
			fail("Should have failed...");
		} catch (InstrumentationException e) {
			// ignore
		}
		try {
			create("TestGettersCodeBad3");
			fail("Should have failed...");
		} catch (InstrumentationException e) {
			// ignore
		}
		try {
			create("TestGettersCodeBad4");
			fail("Should have failed...");
		} catch (InstrumentationException e) {
			// ignore
		}
	}

	@Test
	public void testPeInstantiationAsParameter() throws Exception {
		try {
			create("TestGettersCodeBad5");
			fail("Should have failed...");
		} catch (InstrumentationException e) {
			// ignore
		}
	}
}
