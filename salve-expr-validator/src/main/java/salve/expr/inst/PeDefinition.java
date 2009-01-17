package salve.expr.inst;

import salve.CodeMarker;
import salve.asmlib.Type;

/**
 * Property expression data being tracked by bytecode reader
 * 
 * @author ivaynberg
 * @deprecated
 */
@Deprecated
public class PeDefinition
{
    private Type type;
    private String expression;
    private String mode;
    private CodeMarker marker;

    public PeDefinition()
    {
        clear();
    }

    public void clear()
    {
        type = null;
        expression = null;
        mode = "rw";
        marker = null;
    }

    public String getExpression()
    {
        return expression;
    }

    public CodeMarker getMarker()
    {
        return marker;
    }

    public String getMode()
    {
        return mode;
    }

    public Type getType()
    {
        return type;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public void setMarker(CodeMarker marker)
    {
        this.marker = marker;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("[").append(getClass().getSimpleName()).append(" type=")
                .append(type).append(", expresson=").append(expression).append(", mode=").append(
                        mode).append(", marker=").append(marker).append("]").toString();
    }

}
