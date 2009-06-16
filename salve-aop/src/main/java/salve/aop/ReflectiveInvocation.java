package salve.aop;

import java.lang.reflect.Method;

public class ReflectiveInvocation implements MethodInvocation
{
    private final Object instance;
    private final Method method;

    private final Method executor;
    private Object[] arguments;


    public ReflectiveInvocation(Object instance, Method executor, Method method, Object[] arguments)
    {
        this.instance = instance;
        this.executor = executor;
        this.method = method;
        this.arguments = arguments;
    }


    public Object execute() throws Throwable
    {
        return executor.invoke(instance, arguments);
    }


    public Object getThis()
    {
        return instance;
    }


    public Method getMethod()
    {
        return method;
    }


    public Object[] getArguments()
    {
        return arguments;
    }

}
