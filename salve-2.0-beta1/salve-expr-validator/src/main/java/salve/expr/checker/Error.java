package salve.expr.checker;

import salve.expr.scanner.Expression;

public class Error
{
    private final Expression expression;
    private final String message;

    public Error(Expression expression, String message)
    {
        this.expression = expression;
        this.message = message;
    }

    public Error(String message)
    {
        this(null, message);
    }


    public Expression getExpression()
    {
        return expression;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("\nExpression Error: ").append(message).append("\n  Expression: ")
            .append(expression).toString();
    }
}
