package salve.config.test;

import java.io.InputStream;
import java.util.Iterator;

import junit.framework.TestCase;
import salve.config.xml.ConfigLoader;

public class ConfigReaderTest extends TestCase
{

    public void test()
    {
        InputStream xml = Config.class.getResourceAsStream("config.xml");
        assertNotNull(xml);
        Config config = (Config)new ConfigLoader(getClass().getClassLoader()).read(xml);
        assertNotNull(config);
        assertEquals(2, config.getPackages().size());
        Iterator<Package> packages = config.getPackages().iterator();
        Package p1 = packages.next();
        if ("com.project.data.*".equals(p1.getName()))
        {
            assertPackage1(p1);
            assertPackage2(packages.next());
        }
        else if ("com.project.web.*".equals(p1.getName()))
        {
            assertPackage2(p1);
            assertPackage1(packages.next());
        }
        else
        {
            fail("unknown package name: " + p1.getName());
        }

    }

    /**
     * @param p1
     */
    private static void assertPackage1(Package p1)
    {
        assertEquals("com.project.data.*", p1.getName());
        assertNotNull(p1.getInstrumentors());
        assertEquals(1, p1.getInstrumentors().size());
    }

    /**
     * @param packages
     */
    private void assertPackage2(Package p2)
    {
        assertEquals("com.project.web.*", p2.getName());
        assertNotNull(p2.getInstrumentors());
        assertEquals(2, p2.getInstrumentors().size());
    }
}
