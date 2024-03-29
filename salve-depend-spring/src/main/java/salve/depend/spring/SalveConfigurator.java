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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import salve.depend.DependencyLibrary;

/**
 * A bean that when dropped into a spring context will link DependencyLibrary
 * with that spring context
 * <p>
 * Example spring configuration: {@code  <bean
 * class="salve.depend.spring.SalveConfigurator"/>}
 * </p>
 * 
 * @author ivaynberg
 */
public class SalveConfigurator implements ApplicationContextAware {
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		DependencyLibrary.addLocator(new SpringBeanLocator(context));
	}

}
