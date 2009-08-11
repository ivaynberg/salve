package salve.aop;

import java.lang.reflect.Method;

import salve.aop.inst.BasicAspectsTest.BeanAdvice;

/**
 * Simply here for bytecode exploration at dev time, nothing functional
 * 
 * @author igor.vaynberg
 * 
 */
public class BeanInstrumented
{
  private static final Class[] NO_PARAM_TYPES = new Class[]{null};

  // @Traced
    public Integer hello()
    {
        Object[] args = new Object[0];

        try
        {
            final Class< ? > clazz = getClass();

          final Method executor = clazz.getDeclaredMethod("_salve_aop$hello", NO_PARAM_TYPES);
            final Method method = clazz.getDeclaredMethod("hello", NO_PARAM_TYPES);
            final MethodInvocation invocation = new ReflectiveInvocation(this, executor, method,
                    null);
            try
            {
                return (Integer)BeanAdvice.simple(invocation);
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
            throw new AspectInvocationException(e);
        }
    }

    public int _salve_aop$hello()
    {
        System.out.println("hello");
        return 5;
    }


    public static void main(String[] args)
    {
        new BeanInstrumented().hello();
    }
}
