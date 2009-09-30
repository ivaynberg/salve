package salve.aop;

public class UndeclaredException extends RuntimeException
{

    public UndeclaredException(Throwable cause)
    {
        super("Unknown exception throw from aspect", cause);
    }

}
