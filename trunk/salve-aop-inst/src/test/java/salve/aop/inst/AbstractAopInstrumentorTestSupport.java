package salve.aop.inst;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import salve.Scope;
import salve.aop.AopInstrumentor;
import salve.loader.BytecodePool;

public class AbstractAopInstrumentorTestSupport extends Assert
{
    private static final ClassLoader CL = AbstractAopInstrumentorTestSupport.class.getClassLoader();
    private static final AopInstrumentor INST = new AopInstrumentor();
    private static BytecodePool pool= new BytecodePool(Scope.ALL).addLoaderFor(CL);
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
