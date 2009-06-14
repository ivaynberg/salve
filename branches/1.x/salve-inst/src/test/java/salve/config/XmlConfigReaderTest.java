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
package salve.config;

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import salve.ConfigException;

public class XmlConfigReaderTest {
	@Test
	public void testBrokenConfigs() throws Exception {
		URL url1 = Thread.currentThread().getContextClassLoader().getResource("salve/config/test-broken-config1.xml");
		URL url2 = Thread.currentThread().getContextClassLoader().getResource("salve/config/test-broken-config2.xml");

		Assert.assertNotNull("missing config file", url1);
		Assert.assertNotNull("missing config file", url2);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		try {
			new XmlConfigReader(loader).read(url1.openStream(), new XmlConfig());
			Assert.fail("Should have failed with class not found config exception");
		} catch (ConfigException e) {

		}

		try {
			new XmlConfigReader(loader).read(url2.openStream(), new XmlConfig());
			Assert.fail("Should have failed with instrumentor of invalid type config exception");
		} catch (ConfigException e) {

		}

	}

	@Test
	public void testWorkingConfig() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("salve/config/test-working-config.xml");
		Assert.assertNotNull("missing config file", url);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		XmlConfig config = new XmlConfig();
		new XmlConfigReader(loader).read(url.openStream(), config);

		XmlPackageConfig p1 = config.getPackageConfig("salve.package1");
		Assert.assertNotNull(p1);
		Assert.assertEquals(1, p1.getInstrumentors().size());
		Assert.assertTrue(p1.getInstrumentors().get(0).equals(Instrumentor1.class.getName()));

		XmlPackageConfig p2 = config.getPackageConfig("salve.package2");
		Assert.assertNotNull(p2);
		Assert.assertEquals(2, p2.getInstrumentors().size());
		Assert.assertTrue(p2.getInstrumentors().get(0).equals(Instrumentor2.class.getName()));
		Assert.assertTrue(p2.getInstrumentors().get(1).equals(Instrumentor1.class.getName()));

	}
}
