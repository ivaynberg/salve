package salve.config.xml;

import java.io.InputStream;

import salve.ConfigException;
import salve.Instrumentor;
import salve.config.xml.ConfigLoader;

public class XmlConfigReader {
	private final ClassLoader classloader;

	public XmlConfigReader(ClassLoader classloader) {
		super();
		this.classloader = classloader;
	}

	/**
	 * Reads xml config into the specified config object
	 * 
	 * @param is
	 *            input stream to configuration xml
	 * @throws ConfigException
	 */
	public XmlConfig read(InputStream is) throws ConfigException {
		return read(is, null);
	}

	/**
	 * Reads xml config into the specified config object
	 * 
	 * @param is
	 *            input stream to configuration xml
	 * @param config
	 *            config object
	 * @throws ConfigException
	 */
	public XmlConfig read(InputStream is, XmlConfig existing) throws ConfigException {
		ConfigLoader loader = new ConfigLoader(classloader);
		loader.addAlias("config", XmlConfig.class.getName());
		loader.addAlias("package", XmlPackage.class.getName());
		Object root = loader.read(is);

		if (!XmlConfig.class.isAssignableFrom(root.getClass())) {
			throw new ConfigException("Configuration root object must be of type: " + XmlConfig.class.getName()
					+ ", but was of type: " + root.getClass().getName());
		}

		XmlConfig config = (XmlConfig) root;
		if (config.getPackages().isEmpty()) {
			throw new ConfigException("No package declarations were found, must contain at least one");
		}

		for (XmlPackage pkg : config.getPackages()) {
			final String name = pkg.getName();
			if (name == null || name.trim().isEmpty()) {
				throw new ConfigException("One or more packages do not have a name");
			}
			if (pkg.getInstrumentors().isEmpty()) {
				throw new ConfigException("Package: " + name + " does not contain any instrumentors");
			}
			for (Object inst : pkg.getInstrumentors()) {
				if (!(inst instanceof Instrumentor)) {
					throw new ConfigException("Instrumentor class: " + inst.getClass().getName()
							+ " does not implement: " + Instrumentor.class.getName());
				}
			}
		}

		if (existing != null) {
			existing.getPackages().addAll(config.getPackages());
			existing.initialize(classloader);
			return existing;
		} else {
			return config;
		}
	}
}
