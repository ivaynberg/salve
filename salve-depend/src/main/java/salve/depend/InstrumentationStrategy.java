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
 * Strategy for instrumenting dependency fields.
 * 
 * @see Dependency
 * @see DependencyLibrary
 * 
 * @author ivaynberg
 * 
 */
public enum InstrumentationStrategy {
	/**
	 * The field is made transient and is injected on first read access. Write
	 * access to this field is forbidden and a
	 * {@link IllegalFieldWriteException} exception is thrown when field is
	 * written to.
	 */
	INJECT_FIELD,

	/**
	 * The field is removed. Methods accessing this field will have a local
	 * variable added that will hold the looked up dependency. Removing the
	 * field improves the memory footprint of the class, and since dependency
	 * lookups are cached there should be no significant performance
	 * degradation.
	 * 
	 * This is the default strategy
	 */
	REMOVE_FIELD;
}
