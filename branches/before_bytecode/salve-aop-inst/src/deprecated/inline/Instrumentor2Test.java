package salve.aop.inst.inline;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import salve.Scope;
import salve.aop.MethodAdvice;
import salve.aop.MethodInvocation;
import salve.aop.inst.AbstractAopInstrumentorTestSupport;
import salve.loader.BytecodePool;

public class Instrumentor2Test extends TestCase
{

    @Retention(RetentionPolicy.RUNTIME)
    @MethodAdvice(instrumentorClass = Instrumentor2Test.class, instrumentorMethod = "bye")
    @Inherited
    public @interface Bye {

    }

    public static Object bye(MethodInvocation method) throws Throwable
    {
        String bye = "bye";
        System.out.println(bye);
        return "as";
        // return ((String)method.execute()).toUpperCase();
    }


    @Retention(RetentionPolicy.RUNTIME)
    @MethodAdvice(instrumentorClass = Instrumentor2Test.class, instrumentorMethod = "uppercase")
    @Inherited
    public @interface Uppercase {

    }

    public static Object uppercase(MethodInvocation method) throws Throwable
    {
        System.out.println("i am uppercase aspect before method.execute");
        return ((String)method.execute()).toUpperCase();
        
    }

    public static class Bean
    {
        @Uppercase
        public String echo(String str)
        {
            System.out.println("i am echo method");
            return str;
        }
//
//        public String echo_ordinary(String str)
//        {
//            int a = 2;
//            return str;
//        }
    }

    public void test() throws Exception
    {
        Thread kill = new Thread()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("FORCE EXITTING");
                System.exit(0);
            };
        };
        kill.setDaemon(true);
        kill.start();
        Bean bean = create("Bean");
        System.out.println(bean.echo("hello"));


    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////


    private static final ClassLoader CL = AbstractAopInstrumentorTestSupport.class.getClassLoader();
    private static final AopInstrumentor2 INST = new AopInstrumentor2();
    protected static BytecodePool pool = new BytecodePool(Scope.ALL).addLoaderFor(CL);
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
