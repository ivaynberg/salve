package salve.config.impl;


public class ConfigReader {
	/**
	 * Reads xml config into the specified config object
	 * 
	 * @param is
	 *            input stream to configuration xml
	 * @param config
	 *            config object
	 * @throws ConfigException
	 */
//	public Config read(InputStream is) throws ConfigException {
//		Object root = ConfigLoader.read(is);
//
//		if (Config.class.isAssignableFrom(root.getClass())) {
//			throw new ConfigException("Configuration root object must be of type: " + Config.class.getName()
//					+ ", but was of type: " + root.getClass().getName());
//		}
//
//		Config config = (Config) root;
//		if (config.getPackages().isEmpty()) {
//			throw new ConfigException("No package declarations were found, must contain at least one");
//		}
//
//		for (Package pkg : config.getPackages()) {
//			final String name = pkg.getName();
//			if (name == null || name.trim().isEmpty()) {
//				throw new ConfigException("One or more packages do not have a name");
//			}
//			if (pkg.getInstrumentors().isEmpty()) {
//				throw new ConfigException("Package: " + name + " does not contain any instrumentors");
//			}
//		}
//		return config;
//	}

}
