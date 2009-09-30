package salve.config.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class ConfigLoader
{
    private final ClassLoader classloader;
    private final Map<String, String> aliases = new HashMap<String, String>();

    public ConfigLoader(ClassLoader classloader)
    {
        this.classloader = classloader;
    }


    public ConfigLoader addAlias(String alias, String cn)
    {
        aliases.put(alias, cn);
        return this;
    }

    /**
     * Reads xml config into the specified config object
     * 
     * @param is
     *            input stream to configuration xml
     */
    public Object read(InputStream is)
    {
        SAXParser parser;
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();

            ConfigBuilder builder = new ConfigBuilder(classloader, aliases);
            parser.parse(is, builder);
            return builder.getObject();
        }
        catch (ParserConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        catch (SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
