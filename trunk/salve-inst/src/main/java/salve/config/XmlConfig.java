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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import salve.Config;
import salve.Instrumentor;

/**
 * Simple packaged-scoped configuration that contains mappings of packages to
 * instrumentors
 * 
 * @author ivaynberg
 * 
 */
public class XmlConfig implements Config {
	List<XmlPackageConfig> packageConfigs = new ArrayList<XmlPackageConfig>();

	/**
	 * Constructor
	 */
	public XmlConfig() {
		super();
	}

	/**
	 * Adds a package configuration
	 * 
	 * @param config
	 */
	public void add(XmlPackageConfig config) {
		packageConfigs.add(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Instrumentor> getInstrumentors(String className) {
		XmlPackageConfig conf = getPackageConfig(className.replace("/", "."));
		Collection<Instrumentor> inst = null;

		if (conf != null) {
			inst = conf.getInstrumentors();
			if (inst == null) {
				inst = Collections.emptyList();
			}
		} else {
			inst = Collections.emptyList();
		}
		return inst;
	}

	/**
	 * Retrieves package configuration for a given class name
	 * 
	 * @param name
	 *            class name
	 * @return matching package config or null
	 */
	public XmlPackageConfig getPackageConfig(String name) {
		for (XmlPackageConfig config : packageConfigs) {
			if (name.startsWith(config.getPackageName() + ".") || name.equals(config.getPackageName())) {
				return config;
			}
		}
		return null;
	}

	/**
	 * @return all available package configurations
	 */
	public List<XmlPackageConfig> getPackageConfigs() {
		return packageConfigs;
	}

}
