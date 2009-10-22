package salve.aop.inst;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import salve.loader.TestBytecodePool;

public class AbstractAopInstrumentorTestSupport extends Assert
{
    private static final ClassLoader CL = AbstractAopInstrumentorTestSupport.class.getClassLoader();
    protected static TestBytecodePool pool = new TestBytecodePool(CL);
    private static final AopInstrumentor INST = new AopInstrumentor();
    private static final Map<String, Class< ? >> loaded = new HashMap<String, Class< ? >>();

    @SuppressWarnings("unchecked")
    protected final <T> T create(String beanName) throws Exception
    {
        return (T)instrument(beanName).newInstance();
    }

    protected final Class< ? > instrument(String beanName) throws Exception
    {
        final String cn = getClass().getName().replace(".", "/") + "$" + beanName;
        Class< ? > clazz = loaded.get(cn);
        if (clazz == null)
        {
            clazz = pool.instrumentIntoClass(cn, INST);
            loaded.put(cn, clazz);
        }
        return clazz;
    }

}
