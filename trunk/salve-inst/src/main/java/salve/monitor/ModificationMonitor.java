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
package salve.monitor;

import salve.InstrumentorMonitor;

public class ModificationMonitor implements InstrumentorMonitor {
	private boolean modified = false;

	public void fieldAdded(String className, int fieldAccess, String fieldName, String fieldDesc) {
		modified = true;
	}

	public void fieldModified(String className, int fieldAccess, String fieldName, String fieldDesc) {
		modified = true;
	}

	public void fieldRemoved(String className, int fieldAccess, String fieldName, String fieldDesc) {
		modified = true;
	}

	public boolean isModified() {
		return modified;
	}

	public void methodAdded(String className, int methodAccess, String methodName, String methodDesc) {
		modified = true;
	}

	public void methodModified(String className, int methodAccess, String methodName, String methodDesc) {
		modified = true;
	}

	public void methodRemoved(String className, int methodAccess, String methodName, String methodDesc) {
		modified = true;
	}

}
