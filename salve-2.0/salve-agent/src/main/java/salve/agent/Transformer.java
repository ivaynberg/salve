/**
 * 
 */
package salve.agent;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import salve.config.xml.XmlConfig;
import salve.config.xml.XmlConfigReader;

/**
 * Default class transformer that uses salve's xml configuration
 * 
 * @author ivaynberg
 * 
 */
class Transformer extends AbstractTransformer
{
    /** set of classloaders that were already used to look for salve config */
    private final Set<ClassLoader> seenLoaders = new HashSet<ClassLoader>();

    /** set urls from which a salve config was already loaded */
    private final Set<String> seenUrls = new HashSet<String>();

    /** salve configuration */
    private final XmlConfig config = new XmlConfig();

    @Override
    protected salve.config.xml.XmlConfig getConfig(ClassLoader loader, String className)
    {
        // see if this is a new classloader, and if it is try to load any
        // salve config files
        if (!seenLoaders.contains(loader))
        {
            mergeConfigs(loader);
            seenLoaders.add(loader);
        }

        return config;
    }

    /**
     * Merges Salve config using specified class loader and url into {@link #config}
     * 
     * @param loader
     *            class loader
     * @param url
     *            url to salve2.xml
     */
    private void mergeConfig(ClassLoader loader, URL url)
    {
        try
        {
            XmlConfigReader reader = new XmlConfigReader(loader);

            if (isDebugEnabled())
            {
                System.out.println("Salve:Agent:Loading Salve config from: " + url.toString());
            }

            reader.read(url.openStream(), config);
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Could not read Salve configuration from: " + url.toString(), e);
        }
    }

    /**
     * Searches class loader for any META-INF/salve2.xml resources and merges them into
     * {@link #config}
     * 
     * @param loader
     *            class loader
     */
    private void mergeConfigs(ClassLoader loader)
    {
        Enumeration<URL> urls = null;

        try
        {
            urls = loader.getResources("META-INF/salve2.xml");
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Could not search for Salve configuration files using classloader: " +
                            loader.toString());
        }

        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            final String location = url.toString();
            if (!seenUrls.contains(location))
            {
                mergeConfig(loader, url);
                seenUrls.add(location);
            }
        }
    }

}