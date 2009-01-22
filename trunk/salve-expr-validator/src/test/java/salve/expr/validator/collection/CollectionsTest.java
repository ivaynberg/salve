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
package salve.expr.validator.collection;

import org.junit.Test;

import salve.InstrumentationException;
import salve.expr.Pe;
import salve.expr.TestCaseSupport;
import salve.expr.validator.collection.model.Family;

public class CollectionsTest extends TestCaseSupport {

	public static class CorrectListAccess {
		public void access() {
			new Pe(Family.class, "ancestors.0.name");
		}
	}

	public static class CorrectMapAccess {
		public void access() {
			new Pe(Family.class, "members.uncle.name");
		}
	}

	public static class IncorrectListAccess_BadIndex1 {
		public void access() {
			new Pe(Family.class, "ancestors.0a.name");
		}
	}

	public static class IncorrectListAccess_BadIndex2 {
		public void access() {
			new Pe(Family.class, "ancestors.a0.name");
		}
	}

	public static class IncorrectMapAccess {
		public void access() {
			new Pe(Family.class, "members.uncle.foo");
		}
	}

	@Test
	public void testCorrectListAccess() throws Exception {
		instrument("CorrectListAccess");
	}

	@Test
	public void testCorrectMapAccess() throws Exception {
		instrument("CorrectMapAccess");
	}

	@Test
	public void testIncorrectListAccess() throws Exception {
		try {
			instrument("IncorrectListAccess_BadIndex1");
			fail("Should have failed because list index is bad");
		} catch (InstrumentationException e) {
			// noop
		}

		try {
			instrument("IncorrectListAccess_BadIndex2");
			fail("Should have failed because list index is bad");
		} catch (InstrumentationException e) {
			// noop
		}

	}

	@Test
	public void testIncorrectMapAccess() throws Exception {
		try {
			instrument("IncorrectMapAccess");
			fail("Should have failed since Member does not have foo()");
		} catch (InstrumentationException e) {
			// noop
		}

	}

}
