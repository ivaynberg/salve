package salve.aop.inst.inline;

import java.lang.reflect.Method;

import salve.aop.MethodInvocation;

public class ReflectiveInvocation2 implements MethodInvocation
{
    private final Object instance;
    private final Method method;

    private Object[] arguments;


    public ReflectiveInvocation2(Object instance, Method method, Object[] arguments)
    {
        this.instance = instance;
        this.method = method;
        this.arguments = arguments;
    }


    public Object execute() throws Throwable
    {
        return method.invoke(instance, arguments);
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
