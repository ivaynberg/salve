package salve.expr.inst.locator;

import java.util.List;

public class ExpressionDefinition
{
    private final List<ExpressionPart> parts;

// private int type = -1;
// private int expr = -1;
// private int mode = -1;
// private int _this = -1;

    ExpressionDefinition(List<ExpressionPart> parts)
    {
        this.parts = parts;
        for (int i = 0; i < parts.size(); i++)
        {
            final ExpressionPart part = parts.get(i);
//            switch (part.getType())
//            {
//                case EXPR :
//                    expr = i;
//                    break;
//                case MODE :
//                    mode = i;
//                    break;
//                case TYPE :
//                    type = i;
//                    break;
//                case THIS :
//                    _this = i;
//                    break;
//            }
        }
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
        ExpressionDefinition other = (ExpressionDefinition)obj;
        if (parts == null)
        {
            if (other.parts != null)
                return false;
        }
        else if (!parts.equals(other.parts))
            return false;
        return true;
    }


}
