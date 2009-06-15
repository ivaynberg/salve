package salve.aop;

import java.lang.reflect.Method;

public class BeanInstrumented
{
    @Traced
    public void hello()
    {
        
        try
        {
            final Class<?> clazz=getClass();
            final Method executor = clazz.getDeclaredMethod("_salve_aop$hello", null);
            final Method method = clazz.getDeclaredMethod("hello", null);
            final MethodInvocation invocation = new ReflectiveInvocation(this, executor, method, null);
            TracedAdvice.advise(invocation);
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
    
    
    public void hello(String a, int b) {
        Object[] args=new Object[2];
        args[0]=a;
        args[1]=b;
    }
    
    public static void main(String[] args)
    {
        new BeanInstrumented().hello();
    }
}
