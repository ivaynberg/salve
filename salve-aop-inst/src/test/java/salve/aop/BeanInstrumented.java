package salve.aop;

import java.lang.reflect.Method;

import salve.aop.inst.AopInstrumentorTest.BeanAdvice;

public class BeanInstrumented
{
    // @Traced
    public void hello()
    {
        Object[] args = new Object[0];

        try
        {
            final Class< ? > clazz = getClass();
            final Method executor = clazz.getDeclaredMethod("_salve_aop$hello", null);
            final Method method = clazz.getDeclaredMethod("hello", null);
            final MethodInvocation invocation = new ReflectiveInvocation(this, executor, method,
                    null);
            try
            {
                BeanAdvice.simple(invocation);
            }
            catch (Throwable t)
            {
                if (t instanceof java.lang.reflect.InvocationTargetException)
                {
                    t = ((java.lang.reflect.InvocationTargetException)t).getCause();
                }

                if (t instanceof RuntimeException)
                {
                    throw (RuntimeException)t;
                }
                if (t instanceof IllegalStateException)
                {
                    throw (IllegalStateException)t;
                }
                throw new UndeclaredException(t);
            }
        }
        catch (SecurityException e)
        {
            throw new AspectInvocationException(e);
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _salve_aop$hello()
    {
        System.out.println("hello");
    }


    public static void main(String[] args)
    {
        new BeanInstrumented().hello();
    }
}
