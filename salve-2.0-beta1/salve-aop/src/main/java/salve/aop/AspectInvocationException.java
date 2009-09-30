package salve.aop;

public class AspectInvocationException extends RuntimeException
{

    public AspectInvocationException(Throwable cause)
    {
        super("Unknown exception throw from aspect", cause);
    }

}
