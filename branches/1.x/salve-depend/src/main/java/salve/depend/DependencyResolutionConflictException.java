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
 * Exception thrown when more then one dependency matches the key
 * 
 * @author ivaynberg
 */
public class DependencyResolutionConflictException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private static String message(Key key, String details) {
		StringBuilder error = new StringBuilder();
		error.append("Found two or more beans matching the same key: ").append(key.toString()).append(
				". Either make the key more specific by adding a qualifier annotation, "
						+ "eg salve.depend.spring.SpringBeanId if using spring, "
						+ "or make sure the key will only match a single bean. ").append(details);
		return error.toString();
	}

	public DependencyResolutionConflictException(Key key, String message) {
		super(message(key, message));
	}
}
