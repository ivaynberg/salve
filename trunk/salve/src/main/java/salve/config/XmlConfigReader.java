package salve.config;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import salve.Instrumentor;

public class XmlConfigReader {
	private final ClassLoader instrumentorLoader;

	public XmlConfigReader(ClassLoader instrumentorLoader) {
		this.instrumentorLoader = instrumentorLoader;
	}

	public void read(InputStream is, Config config) throws ConfigException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, new Handler(config));
		} catch (RTConfigException e) {
			throw e.toConfigException();
		} catch (Exception e) {
			throw new ConfigException("Could not read configuration", e);
		}
	}

	private class Handler extends DefaultHandler {
		private final Config config;
		private PackageConfig pconfig;

		public Handler(Config config) {
			super();
			this.config = config;
		}

		@Override public void endElement(String uri, String localName, String name) throws SAXException {
			if ("package".equals(name)) {
				onEndPackage();
			}
		}

		@Override public void startElement(String uri, String localName, String name, Attributes attributes)
				throws SAXException {
			if ("package".equals(name)) {
				String packageName = attributes.getValue("name");
				onStartPackage(packageName);
			} else if ("instrumentor".equals(name)) {
				String instClassName = attributes.getValue("class");
				onInstrumentor(instClassName);
			}

		}

		/**
		 * 
		 */
		private void onEndPackage() {
			config.add(pconfig);
		}

		/**
		 * @param instClassName
		 */
		private void onInstrumentor(String instClassName) {
			Class instClass = null;
			try {
				instClass = instrumentorLoader.loadClass(instClassName);
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

			pconfig.add((Instrumentor) inst);
		}

		/**
		 * @param packageName
		 */
		private void onStartPackage(String packageName) {
			pconfig = new PackageConfig();
			pconfig.setPackageName(packageName);
		}
	}

	private static class RTConfigException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public RTConfigException(String message) {
			super(message);
		}

		public RTConfigException(String message, Throwable cause) {
			super(message, cause);
		}

		public ConfigException toConfigException() {
			return new ConfigException(getMessage(), getCause());
		}

	}

}
