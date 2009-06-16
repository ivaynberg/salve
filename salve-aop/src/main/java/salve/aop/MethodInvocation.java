package salve.aop;

import java.lang.reflect.Method;

public interface MethodInvocation
{
    Object execute() throws Throwable;

    Object getThis();

    Method getMethod();
    
    Object[] getArguments();
}
