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

import salve.asmlib.Method;
import salve.asmlib.Type;
import salve.contract.pe.Arg;

public interface Constants {
	static final Type NOTNULL = Type.getType("Lsalve/contract/NotNull;");
	static final Type NOTEMPTY = Type.getType("Lsalve/contract/NotEmpty;");

	static final Type ILLEGALARGEX = Type.getType(IllegalArgumentException.class);
	static final Method ILLEGALARGEX_INIT = new Method("<init>", "(Ljava/lang/String;)V");

	static final Type ILLEGALSTATEEX = Type.getType(IllegalStateException.class);

	static final Method ILLEGALSTATEEX_INIT = new Method("<init>", "(Ljava/lang/String;)V");

	static final Type STRING_TYPE = Type.getType(String.class);
	static final Method STRING_TRIM_METHOD = new Method("trim", "()Ljava/lang/String;");
	static final Method STRING_LENGTH_METHOD = new Method("length", "()I");

	static final Type OMI = Type.getType("Lsalve/contract/OverrideMustInvoke;");

	static final Type GE0 = Type.getType("Lsalve/contract/GE0;");
	static final Type GT0 = Type.getType("Lsalve/contract/GT0;");
	static final Type LT0 = Type.getType("Lsalve/contract/LT0;");
	static final Type LE0 = Type.getType("Lsalve/contract/LE0;");
	static final Type PE = Type.getType("Lsalve/contract/PE;");
	static final Arg[] PE_INIT = new Arg[] { Arg.TYPE, Arg.EXPRESSION, Arg.MODE };

}
