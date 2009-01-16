package salve.expr.inst.locator;


class Instruction
{
    static enum Type {
        THIS,
        OTHER,
        TYPE,
        STRING
    };

    static Instruction THIS = new Instruction(Type.THIS, null);
    static Instruction OTHER = new Instruction(Type.OTHER, null);

    private final Type type;
    private final String data;

    Instruction(Type type, String data)
    {
        this.type = type;
        this.data = data;
    }


    public Type getType()
    {
        return type;
    }


    public String getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return new StringBuilder("[").append(getClass().getSimpleName()).append(" type=").append(
                type).append(", data=").append(data).append("]").toString();
    }


    public boolean matches(Part part)
    {
        switch (type)
        {
            case OTHER :
                return Part.OTHER.equals(part);
            case STRING :
                return Part.EXPR.equals(part) || Part.MODE.equals(part);
            case THIS :
                return Part.THIS.equals(part);
            case TYPE :
                return Part.TYPE.equals(part);
            default :
                return false;
        }
    }


}
