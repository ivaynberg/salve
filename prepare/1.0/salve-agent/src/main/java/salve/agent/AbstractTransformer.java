package salve.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Iterator;

import salve.CodeMarker;
import salve.CodeMarkerAware;
import salve.Config;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.MemoryLoader;
import salve.monitor.NoopMonitor;

/**
 * Base class for class transformers. This class takes care of everything except loading salve
 * configuration ({@link Config})
 * 
 * @author ivaynberg
 * 
 */
public abstract class AbstractTransformer implements ClassFileTransformer
{
    /**
     * Gets configuration for the given class name and class loader
     * 
     * @param loader
     * @param className
     * @return {@link Config} for class
     */
    protected abstract Config getConfig(ClassLoader loader, String className);

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
    private byte[] instrument(ClassLoader loader, String className, byte[] bytecode)
    {
        final Config config = getConfig(loader, className);
        try
        {
            Collection<Instrumentor> instrumentors = config.getInstrumentors(className);

            printDebugInfoIfNecessary(className, instrumentors);

            for (Instrumentor inst : instrumentors)
            {
                CompoundLoader bl = new CompoundLoader();
                bl.addLoader(new MemoryLoader(className, bytecode));
                bl.addLoader(new ClassLoaderLoader(loader));

                InstrumentationContext ctx = new InstrumentationContext(bl, NoopMonitor.INSTANCE,
                        config.getScope(inst));

                bytecode = inst.instrument(className, ctx);
            }
            return bytecode;
        }
        catch (InstrumentationException e)
        {
            int line = 0;
            if (e instanceof CodeMarkerAware)
            {
                CodeMarker marker = ((CodeMarkerAware)e).getCodeMarker();
                if (marker != null)
                {
                    line = Math.max(0, marker.getLineNumber());
                }
            }
            StringBuilder message = new StringBuilder();
            message.append("Could not instrument ").append(className).append(".");
            if (line > 0)
            {
                message.append(" Error on line: ").append(line);
            }
            throw new RuntimeException(message.toString(), e);
        }
    }

    protected boolean isDebugEnabled()
    {
        return "true".equals(System.getProperty("salve.agent.debug"));
    }

    private void printDebugInfoIfNecessary(String className, Collection<Instrumentor> instrumentors)
    {
        if (isDebugEnabled())
        {
            System.out.print("Salve:Agent:Instrumenting " + className + " using [");
            Iterator<Instrumentor> it = instrumentors.iterator();
            while (it.hasNext())
            {
                System.out.print(it.next().getClass().getName());
                if (it.hasNext())
                {
                    System.out.print(", ");
                }
            }
            System.out.println("]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[] transform(ClassLoader loader, String className, Class< ? > classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException
    {

        // instrument bytecode
        return instrument(loader, className, classfileBuffer);
    }
}
