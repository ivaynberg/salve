package salve.expr.inst.locator;

import salve.asmlib.Type;

public class Rule
{
    private final Part[] parts;
    private final Type container;

    public Rule(Type container, Part... parts)
    {
        this.container = container;
        this.parts = parts;
    }

    public Rule(String container, Part... parts)
    {
        this.container = Type.getObjectType(container);
        this.parts = parts;
    }


    public Type getContainer()
    {
        return container;
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parts == null) ? 0 : parts.hashCode());
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
        Rule other = (Rule)obj;
        if (parts == null)
        {
            if (other.parts != null)
                return false;
        }
        else if (!parts.equals(other.parts))
            return false;
        return true;
    }


    public Part[] getParts()
    {
        return parts;
    }


}
