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
package salve.contract.impl;

import salve.contract.GE0;
import salve.contract.GT0;
import salve.contract.LE0;
import salve.contract.LT0;
import salve.contract.NotEmpty;
import salve.contract.NotNull;
import salve.contract.OverrideMustInvoke;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.Method;

public interface Constants {
	static final Type NOTNULL = Type.getType(NotNull.class);
	static final Type NOTEMPTY = Type.getType(NotEmpty.class);

	static final Type ILLEGALARGEX = Type.getType(IllegalArgumentException.class);
	static final Method ILLEGALARGEX_INIT = new Method("<init>", "(Ljava/lang/String;)V");

	static final Type ILLEGALSTATEEX = Type.getType(IllegalStateException.class);

	static final Method ILLEGALSTATEEX_INIT = new Method("<init>", "(Ljava/lang/String;)V");

	static final Type STRING_TYPE = Type.getType(String.class);
	static final Method STRING_TRIM_METHOD = new Method("trim", "()Ljava/lang/String;");
	static final Method STRING_LENGTH_METHOD = new Method("length", "()I");

	static final Type OMI = Type.getType(OverrideMustInvoke.class);

	static final Type GE0 = Type.getType(GE0.class);
	static final Type GT0 = Type.getType(GT0.class);
	static final Type LT0 = Type.getType(LT0.class);
	static final Type LE0 = Type.getType(LE0.class);

}
