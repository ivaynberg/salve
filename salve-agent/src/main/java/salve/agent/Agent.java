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

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import salve.InstrumentationException;
import salve.Instrumentor;
import salve.config.XmlConfig;
import salve.config.XmlConfigReader;
import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.MemoryLoader;
import salve.monitor.NoopMonitor;

/**
 * Jvm agent that applies Salve instrumentors to classes as they are being
 * loaded.
 * <p>
 * To configure add the following jvm parameter to the command line:
 * <code>-javaagent:path-to-this-jar</code>
 * </p>
 * 
 * @author ivaynberg
 */
public class Agent {
	private static Instrumentation INSTRUMENTATION;

	private static class Transformer implements ClassFileTransformer {
		/** set of classloaders that were already used to look for salve config */
		private final Set<ClassLoader> seenLoaders = new HashSet<ClassLoader>();

		/** set urls from which a salve config was already loaded */
		private final Set<String> seenUrls = new HashSet<String>();

		/** salve configuration */
		private final XmlConfig config = new XmlConfig();

		/**
		 * {@inheritDoc}
		 */
		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {

			// see if this is a new classloader, and if it is try to load any
			// salve config files
			if (!seenLoaders.contains(loader)) {
				mergeConfigs(loader);
				seenLoaders.add(loader);
			}

			// instrument bytecode
			return instrument(loader, className, classfileBuffer);
		}

		/**
		 * Instruments class
		 * 
		 * @param loader
		 *            class loader
		 * @param className
		 *            binary class name (eg salve/agent/Agent)
		 * @param bytecode
		 *            bytecode
		 * @return instrumented bytecode
		 */
		private byte[] instrument(ClassLoader loader, String className,
				byte[] bytecode) {
			try {
				for (Instrumentor inst : config.getInstrumentors(className)) {
					CompoundLoader bl = new CompoundLoader();
					bl.addLoader(new MemoryLoader(className, bytecode));
					bl.addLoader(new ClassLoaderLoader(loader));
					bytecode = inst.instrument(className, bl,
							NoopMonitor.INSTANCE);
				}
				return bytecode;
			} catch (InstrumentationException e) {
				throw new RuntimeException("Error instrumenting class: "
						+ className, e);
			}
		}

		/**
		 * Merges Salve config using specified class loader and url into
		 * {@link #config}
		 * 
		 * @param loader
		 *            class loader
		 * @param url
		 *            url to salve.xml
		 */
		private void mergeConfig(ClassLoader loader, URL url) {
			try {
				XmlConfigReader reader = new XmlConfigReader(loader);
				reader.read(url.openStream(), config);
			} catch (Exception e) {
				throw new RuntimeException(
						"Could not read Salve configuration from: "
								+ url.toString(), e);
			}
		}

		/**
		 * Searches class loader for any META-INF/salve.xml resources and merges
		 * them into {@link #config}
		 * 
		 * @param loader
		 *            class loader
		 */
		private void mergeConfigs(ClassLoader loader) {
			Enumeration<URL> urls = null;

			try {
				urls = loader.getResources("META-INF/salve.xml");
			} catch (IOException e) {
				throw new RuntimeException(
						"Could not search for Salve configuration files using classloader: "
								+ loader.toString());
			}

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				final String location = url.toString();
				if (!seenUrls.contains(location)) {
					mergeConfig(loader, url);
					seenUrls.add(location);
				}
			}
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
