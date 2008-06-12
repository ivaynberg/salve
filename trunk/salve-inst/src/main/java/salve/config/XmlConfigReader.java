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

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import salve.ConfigException;
import salve.util.StreamsUtil;

/**
 * Reads config data from an xml file into a {@link XmlConfig} object
 * 
 * @author ivaynberg
 * 
 */
public class XmlConfigReader {
	private class Handler extends DefaultHandler {
		private final XmlConfig config;
		private XmlPackageConfig pconfig;

		public Handler(XmlConfig config) {
			super();
			this.config = config;
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if ("package".equals(name)) {
				onEndPackage();
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
			pconfig.add(instClassName);
		}

		/**
		 * @param packageName
		 */
		private void onStartPackage(String packageName) {
			pconfig = new XmlPackageConfig();
			pconfig.setPackageName(packageName);
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if ("package".equals(name)) {
				String packageName = attributes.getValue("name");
				onStartPackage(packageName);
			} else if ("instrumentor".equals(name)) {
				String instClassName = attributes.getValue("class");
				onInstrumentor(instClassName);
			}

		}
	}

	public static class RTConfigException extends RuntimeException {

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

	private final ClassLoader instrumentorClassLoader;

	/**
	 * Constructor
	 * 
	 * @param instrumentorClassLoader
	 *            class loader that can be used to load instrumentor classes
	 *            specified in the xml config
	 */
	public XmlConfigReader(ClassLoader instrumentorClassLoader) {
		this.instrumentorClassLoader = instrumentorClassLoader;
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
	public void read(InputStream is, XmlConfig config) throws ConfigException {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, new Handler(config));
			config.initialize(instrumentorClassLoader);
		} catch (RTConfigException e) {
			throw e.toConfigException();
		} catch (Exception e) {
			throw new ConfigException("Could not read configuration", e);
		} finally {
			StreamsUtil.close(is, "Could not close xml config stream");
		}
	}

}
