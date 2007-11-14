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
import java.util.List;

import salve.Instrumentor;

/**
 * A package-scoped cofiguration
 * 
 * @author ivaynberg
 */
public class XmlPackageConfig {
	private String packageName;
	private final List<Instrumentor> instrumentors = new ArrayList<Instrumentor>();

	/**
	 * Adds instrumentor to this package configuration
	 * 
	 * @param instrumentor
	 */
	public void add(Instrumentor instrumentor) {
		instrumentors.add(instrumentor);
	}

	/**
	 * @return list of instrumentors in this config
	 */
	public List<Instrumentor> getInstrumentors() {
		return instrumentors;
	}

	/**
	 * 
	 * @return package name of this config
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Sets package name of this config
	 * 
	 * @param packageName
	 *            package name
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
