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
package salve.contract.pe.map;

import org.junit.Test;

import salve.InstrumentationException;
import salve.contract.AbstractContractInstrumentorTestSupport;
import salve.contract.PE;

public class PEMapsContractInstrumentorTest extends AbstractContractInstrumentorTestSupport {

	public static class CorrectListAccess {
		public void access() {
			new PE(Family.class, "members.ancestors.0.name");
		}
	}

	public static class CorrectMapAccess {
		public void access() {
			new PE(Family.class, "members.uncle.name");
		}
	}

	public static class IncorrectMapAccess {
		public void access() {
			new PE(Family.class, "members.uncle.foo");
		}
	}

	@Test
	public void testCorrectListAccess() throws Exception {
		instrument("CorrectListAccess");
	}

	public void testCorrectMapAccess() throws Exception {
		instrument("CorrectMapAccess");
	}

	public void testIncorrectMapAccess() throws Exception {
		try {
			instrument("IncorrectMapAccess");
			fail("Should have failed since Member does not have foo()");
		} catch (InstrumentationException e) {
			// noop
		}

	}
}
