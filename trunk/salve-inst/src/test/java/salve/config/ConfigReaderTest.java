package salve.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import salve.config.xml.XmlConfigReader;

public class ConfigReaderTest {

	@Test
	public void testBrokenConfig_InvalidInstrumentorClass() throws Exception {
		try {
			new XmlConfigReader(getClass().getClassLoader()).read(getClass().getResourceAsStream(
					"test-broken-config-invalid-instrumentor-class.xml"));
			fail();
		} catch (Exception e) {
			// noop
		}
	}

	@Test
	public void testBrokenConfig_NoPackages() throws Exception {
		try {
			new XmlConfigReader(getClass().getClassLoader()).read(getClass().getResourceAsStream(
					"test-broken-config-no-packages.xml"));
			fail();
		} catch (Exception e) {
			// noop
		}
	}

	@Test
	public void testBrokenConfig_UnknownInstrumentorClass() throws Exception {
		try {
			new XmlConfigReader(getClass().getClassLoader()).read(getClass().getResourceAsStream(
					"test-broken-config-unknown-instrumentor-class.xml"));
			fail();
		} catch (Exception e) {
			// noop
		}
	}

	@Test
	public void testWorkingConfig() throws Exception {
		assertNotNull(new XmlConfigReader(getClass().getClassLoader()).read(getClass().getResourceAsStream(
				"test-working-config.xml")));

	}

	@Test
	public void testWorkingConfig_WithAliases() throws Exception {
		assertNotNull(new XmlConfigReader(getClass().getClassLoader()).read(getClass().getResourceAsStream(
				"test-working-config-with-aliases.xml")));

	}

}
