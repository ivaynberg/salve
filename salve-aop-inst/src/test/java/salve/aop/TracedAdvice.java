package salve.aop;

import salve.aop.MethodInvocation;

public class TracedAdvice
{
    public static Object advise(MethodInvocation invocation) throws Throwable
    {
        System.out.println("Entered traced");
        Object ret = invocation.execute();
        System.out.println("Exitted traced");
        return ret;
    }
}
