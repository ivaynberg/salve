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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import salve.Config;
import salve.Instrumentor;
import salve.Scope;
import salve.config.XmlConfigReader.RTConfigException;

/**
 * Simple packaged-scoped configuration that contains mappings of packages to
 * instrumentors
 * 
 * @author ivaynberg
 * 
 */
public class XmlConfig implements Config {
	private final List<XmlPackageConfig> packageConfigs = new ArrayList<XmlPackageConfig>();
	private final Map<String, Instrumentor> instrumentors = new ConcurrentHashMap<String, Instrumentor>();
	private final Map<Instrumentor, PackageScope> scopes = new ConcurrentHashMap<Instrumentor, PackageScope>();

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
	void add(XmlPackageConfig config) {
		packageConfigs.add(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Instrumentor> getInstrumentors(String className) {
		XmlPackageConfig conf = getPackageConfig(className.replace("/", "."));
		Collection<Instrumentor> inst = null;

		if (conf != null) {
			Collection<String> instClassNames = conf.getInstrumentors();
			if (instClassNames != null) {
				inst = new ArrayList<Instrumentor>(conf.getInstrumentors().size());
				for (String instClassName : conf.getInstrumentors()) {
					inst.add(instrumentors.get(instClassName));
				}
			} else {
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

	/** {@inheritDoc} */
	public Scope getScope(Instrumentor instrumentor) {
		Scope scope = scopes.get(instrumentor);
		if (scope == null) {
			scope = Scope.NONE;
		}
		return scope;
	}

	/**
	 * Initializes the config. This method should be called before
	 * {@link Config} object is about to be used to resolve instrumentors and
	 * after the last {@link #add(XmlPackageConfig)} call.
	 */
	void initialize(ClassLoader instrumentorClassLoader) {
		for (XmlPackageConfig config : packageConfigs) {

			for (String instrumentorClassName : config.getInstrumentors()) {

				Instrumentor instance = instrumentors.get(instrumentorClassName);

				// instantiate instrumentor
				if (instance == null) {
					instance = instantiateInstrumentor(instrumentorClassName, instrumentorClassLoader);
					instrumentors.put(instrumentorClassName, instance);
				}

				// update instrumentor scope
				PackageScope scope = scopes.get(instance);
				if (scope == null) {
					scope = new PackageScope();
					scopes.put(instance, scope);
				}

				scope.addPackage(config.getPackageName());
			}

		}

	}

	private Instrumentor instantiateInstrumentor(String instClassName, ClassLoader instrumentorClassLoader) {
		Class<?> instClass = null;
		try {
			instClass = instrumentorClassLoader.loadClass(instClassName);
		} catch (ClassNotFoundException e) {
			throw new RTConfigException("Could not load instrumentor class " + instClassName
					+ ", make sure it is available on the classpath at the time of instrumentation");
		}

		Object inst;
		try {
			inst = instClass.newInstance();
		} catch (InstantiationException e) {
			throw new RTConfigException("Could not instantiate instrumentor of class " + instClassName, e);
		} catch (IllegalAccessException e) {
			throw new RTConfigException("Could not access instrumentor of class " + instClassName, e);
		}

		if (!(inst instanceof Instrumentor)) {
			throw new RTConfigException(String.format("Instrumentor class %s does not implement %s", instClassName,
					Instrumentor.class.getName()));
		}

		return (Instrumentor) inst;
	}

}
