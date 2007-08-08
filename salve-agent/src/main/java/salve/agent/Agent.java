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
package salve.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import salve.Instrumentor;
import salve.config.Config;
import salve.config.PackageConfig;
import salve.config.XmlConfigReader;
import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.MemoryLoader;
import salve.monitor.NoopMonitor;

public class Agent {
	private static Instrumentation INSTRUMENTATION;

	private static class Transformer implements ClassFileTransformer {
		private final Set<ClassLoader> seenLoaders = new HashSet<ClassLoader>();
		private final Set<URL> seenUrls = new HashSet<URL>();
		private final Config config = new Config();

		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {

			if (!seenLoaders.contains(loader)) {
				XmlConfigReader reader = new XmlConfigReader(loader);
				try {
					Enumeration<URL> urls = loader
							.getResources("META-INF/salve.xml");

					while (urls.hasMoreElements()) {
						URL url = urls.nextElement();
						if (!seenUrls.contains(url)) {
							reader.read(url.openStream(), config);
							seenUrls.add(url);
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(
							"Could not process salve configuration files", e);
				}
				seenLoaders.add(loader);
			}

			try {
				final String name = className.replace("/", ".");
				PackageConfig conf = config.getPackageConfig(name);
				if (conf != null) {
					byte[] bytecode = classfileBuffer;
					for (Instrumentor inst : conf.getInstrumentors()) {
						CompoundLoader bl = new CompoundLoader();
						bl.addLoader(new MemoryLoader(className, bytecode));
						bl.addLoader(new ClassLoaderLoader(loader));
						bytecode = inst.instrument(className, bl,
								NoopMonitor.INSTANCE);
					}
					return bytecode;
				}
			} catch (Exception e) {
				// TODO debug below
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			return classfileBuffer;
		}
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		// ignore double agents
		if (INSTRUMENTATION == null) {
			INSTRUMENTATION = inst;
			inst.addTransformer(new Transformer());
		}
	}

}
