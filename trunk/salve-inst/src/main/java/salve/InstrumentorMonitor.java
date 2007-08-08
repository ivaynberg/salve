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
package salve;

public interface InstrumentorMonitor {

	// these ACC constants parallel asm so we can simply pass in an int from asm
	// visitors into this monitor
	static int ACC_PUBLIC = 0x0001; // class, field, method
	static int ACC_PRIVATE = 0x0002; // class, field, method
	static int ACC_PROTECTED = 0x0004; // class, field, method
	static int ACC_STATIC = 0x0008; // field, method
	static int ACC_FINAL = 0x0010; // class, field, method
	static int ACC_SUPER = 0x0020; // class
	static int ACC_SYNCHRONIZED = 0x0020; // method
	static int ACC_VOLATILE = 0x0040; // field
	static int ACC_BRIDGE = 0x0040; // method
	static int ACC_VARARGS = 0x0080; // method
	static int ACC_TRANSIENT = 0x0080; // field
	static int ACC_NATIVE = 0x0100; // method
	static int ACC_INTERFACE = 0x0200; // class
	static int ACC_ABSTRACT = 0x0400; // class, method
	static int ACC_STRICT = 0x0800; // method
	static int ACC_SYNTHETIC = 0x1000; // class, field, method
	static int ACC_ANNOTATION = 0x2000; // class
	static int ACC_ENUM = 0x4000; // class(?) field inner

	void fieldAdded(String className, int fieldAccess, String fieldName, String fieldDesc);

	void fieldModified(String className, int fieldAccess, String fieldName, String fieldDesc);

	void fieldRemoved(String className, int fieldAccess, String fieldName, String fieldDesc);

	void methodAdded(String className, int methodAccess, String methodName, String methodDesc);

	void methodModified(String className, int methodAccess, String methodName, String methodDesc);

	void methodRemoved(String className, int methodAccess, String methodName, String methodDesc);

}
