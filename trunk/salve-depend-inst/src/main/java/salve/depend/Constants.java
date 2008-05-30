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
package salve.depend;

/**
 * INTERNAL
 * <p>
 * Constants used by {@link ClassInstrumentor}
 * </p>
 * 
 * @author ivaynberg
 * 
 */
interface Constants {
	public static final String NS = "_salve";
	public static final String DEPNS = NS + "dep";
	public static final String REMOVED_FIELD_PREFIX = DEPNS + "rmfld$";
	public static final String KEY_FIELD_PREFIX = DEPNS + "key$";
	public static final String FIELDINIT_METHOD_PREFIX = DEPNS + "fldinit$";
	public static final String CLINIT_METHOD_PREFIX = DEPNS + "clinit$";

	public static final String KEYIMPL_NAME = "salve/depend/KeyImpl";
	public static final String KEYIMPL_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V";
	public static final String DEPLIB_NAME = "salve/depend/DependencyLibrary";
	public static final String KEY_NAME = "salve/depend/Key";
	public static final String KEY_DESC = "L" + KEY_NAME + ";";
	public static final String DEPLIB_LOCATE_METHOD_DESC = "(Lsalve/depend/Key;)Ljava/lang/Object;";
	public static final String DEPLIB_LOCATE_METHOD = "locate";
	public static final String IFWE_NAME = "salve/depend/IllegalFieldWriteException";
	public static final String IFWE_INIT_DESC = "(Ljava/lang/String;Ljava/lang/String;)V";

	public static final String DEP_DESC = "Lsalve/depend/Dependency;";

	public static final String STRAT_REMOVE = "REMOVE_FIELD";
	public static final String STRAT_INJECT = "INJECT_FIELD";

}
