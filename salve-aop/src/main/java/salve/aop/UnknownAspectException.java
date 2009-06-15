package salve.aop;

public class UnknownAspectException extends RuntimeException
{

    public UnknownAspectException(Throwable cause)
    {
        super("Unknown exception throw from aspect", cause);
    }

}
