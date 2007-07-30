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

public class Agent {
	private static Instrumentation INSTRUMENTATION;

	public static void premain(String agentArgs, Instrumentation inst) {
		// ignore double agents
		if (INSTRUMENTATION == null) {
			INSTRUMENTATION = inst;
			inst.addTransformer(new Transformer());
		}
	}

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
						bytecode = inst.instrument(loader, className, bytecode);
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

}
