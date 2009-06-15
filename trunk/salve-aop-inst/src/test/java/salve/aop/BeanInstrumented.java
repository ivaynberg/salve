package salve.aop;

import java.lang.reflect.Method;

public class BeanInstrumented
{
    @Traced
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
                TracedAdvice.advise(invocation);
            }
            catch (Throwable t)
            {
                if (t instanceof RuntimeException)
                {
                    throw (RuntimeException)t;
                }
                if (t instanceof IllegalStateException)
                {
                    throw (IllegalStateException)t;
                }
                throw new UnknownAspectException(t);
            }
        }
        catch (SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
