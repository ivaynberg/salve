package salve.expr.checker;

public class CheckerException extends Exception
{
    private final Error error;

    public CheckerException(Error error)
    {
        super(error.toString());
        this.error = error;
    }

    public Error getError()
    {
        return error;
    }
}
