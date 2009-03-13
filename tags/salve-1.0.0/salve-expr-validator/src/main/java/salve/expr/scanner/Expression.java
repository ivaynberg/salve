package salve.expr.scanner;

import salve.CodeMarker;
import salve.asmlib.Type;

public class Expression
{
    private final Type type;
    private final String path;
    private final String mode;
    private final CodeMarker location;

    public Expression(Type type, String expression, String mode)
    {
        this(type, expression, mode, null);
    }


    public Expression(Type type, String expression, String mode, CodeMarker location)
    {
        this.type = type;
        this.path = expression;
        this.mode = mode;
        this.location = location;
    }


    public CodeMarker getLocation()
    {
        return location;
    }


    public String getPath()
    {
        return path;
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
            type.getInternalName()).append(", expression=").append(path).append(", mode=").append(
            mode).append(", location=").append(location).append("]").toString();
    }


}
