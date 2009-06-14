/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package salve.depend.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import salve.depend.Dependency;

/**
 * Annotation used in conjunction with {@link Dependency} to provide spring
 * bean's id if the type alone is too ambiguous or if resolution by id is
 * preferred
 * <p>
 * Example:
 * 
 * <pre>
 * [at]Dependency
 * [at]SpringId(&quot;services.email&quot;)
 * private EmailService email;
 * 
 * 
 * &lt;bean id=&quot;services.email&quot; class=&quot;EmailServiceImpl&quot;&gt;
 * ...
 * &lt;/bean&gt;
 * 
 * </pre>
 * 
 * </p>
 * @author ivaynberg
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SpringBeanId {
	String value();
}
