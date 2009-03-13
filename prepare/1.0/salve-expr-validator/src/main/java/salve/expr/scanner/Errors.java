package salve.expr.scanner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import salve.expr.checker.Error;

public class Errors implements Iterable<Error>
{
    private final List<Error> errors = new ArrayList<Error>();

    public Errors report(Error error)
    {
        errors.add(error);
        return this;
    }

    public Errors report(Expression expr, String message)
    {
        return report(new Error(expr, message));
    }

    public Iterator<Error> iterator()
    {
        return errors.iterator();
    }

    public boolean isEmpty()
    {
        return errors.isEmpty();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (Error error : errors)
        {
            builder.append(error).append("\n");
        }
        return builder.toString();
    }
}
