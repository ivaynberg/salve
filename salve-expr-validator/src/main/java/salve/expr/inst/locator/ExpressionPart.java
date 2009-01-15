package salve.expr.inst.locator;


public class ExpressionPart
{
    static enum PartType {
        THIS,
        OTHER,
        TYPE,
        EXPR,
        MODE
    };

    static ExpressionPart THIS = new ExpressionPart(PartType.THIS, null);
    static ExpressionPart OTHER = new ExpressionPart(PartType.OTHER, null);

    private final PartType type;
    private final String data;

    ExpressionPart(PartType type, String data)
    {
        this.type = type;
        this.data = data;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpressionPart other = (ExpressionPart)obj;
        if (data == null)
        {
            if (other.data != null)
                return false;
        }
        else if (!data.equals(other.data))
            return false;
        if (type == null)
        {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        return true;
    }


}
