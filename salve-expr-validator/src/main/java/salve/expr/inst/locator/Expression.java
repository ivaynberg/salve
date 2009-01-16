package salve.expr.inst.locator;

import salve.asmlib.Type;

public class Expression
{
    private final Type type;
    private final String expression;
    private final String mode;

    public Expression(Type type, String expression, String mode)
    {
        this.type = type;
        this.expression = expression;
        this.mode = mode;
    }

    public String getExpression()
    {
        return expression;
    }

    public String getMode()
    {
        return mode;
    }

    public Type getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return new StringBuilder("[").append(getClass().getSimpleName()).append(" type=").append(
                type.getInternalName()).append(", expression=").append(expression)
                .append(", mode=").append(mode).append("]").toString();
    }


}
