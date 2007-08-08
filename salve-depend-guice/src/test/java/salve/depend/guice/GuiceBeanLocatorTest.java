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
package salve.depend.guice;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import salve.depend.DependencyInstrumentor;
import salve.depend.DependencyLibrary;
import salve.depend.Locator;
import salve.depend.guice.model.Blue;
import salve.depend.guice.model.Injected;
import salve.depend.guice.model.MockService;
import salve.loader.BytecodePool;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class GuiceBeanLocatorTest {
	private static final String BEAN_NAME = "salve/depend/guice/model/Injected";
	private static Injector injector;
	private static Object injected;
	private static Locator locator;

	@Test
	public void testLookupByType() {
		MockService ts = ((Injected) injected).getTestService();
		Assert.assertNotNull(ts);
		Assert.assertEquals(ts.getName(), ts.getClass().getName());
	}

	@Test
	public void testLookupByTypeAndAnnot() {
		MockService ts = ((Injected) injected).getBlueTestService();
		Assert.assertNotNull(ts);
		Assert.assertEquals(ts.getName(), "BlueTestService");
	}

	@Test
	public void testToString() {
		Assert.assertNotNull(locator.toString());
	}

	@BeforeClass
	public static void init() throws Exception {
		Module module = new AbstractModule() {

			@Override
			protected void configure() {
				bind(MockService.class).in(Scopes.SINGLETON);

				Key<MockService> blueKey = Key.get(MockService.class,
						Blue.class);
				bind(blueKey).toInstance(new MockService("BlueTestService"));

			}

		};

		injector = Guice.createInjector(module);
		locator = new GuiceBeanLocator(injector);
		DependencyLibrary.clear();

		DependencyLibrary.addLocator(locator);

		ClassLoader loader = GuiceBeanLocatorTest.class.getClassLoader();
		BytecodePool pool = new BytecodePool().addLoaderFor(loader);
		Class<?> clazz = pool.instrumentIntoClass(BEAN_NAME,
				new DependencyInstrumentor());
		injected = clazz.newInstance();
	}
}
