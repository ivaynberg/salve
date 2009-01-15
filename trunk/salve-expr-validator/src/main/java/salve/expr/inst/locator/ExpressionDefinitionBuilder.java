package salve.expr.inst.locator;

import java.util.ArrayList;
import java.util.List;

import salve.asmlib.Type;

public class ExpressionDefinitionBuilder
{
    private List<ExpressionPart> parts = new ArrayList<ExpressionPart>();

    public ExpressionDefinitionBuilder addThis()
    {
        parts.add(ExpressionPart.THIS);
        return this;
    }

    public ExpressionDefinitionBuilder addType(Type type)
    {
        parts.add(new ExpressionPart(ExpressionPart.PartType.TYPE, type.getInternalName()));
        return this;
    }

    public ExpressionDefinitionBuilder addExpr(String expr)
    {
        parts.add(new ExpressionPart(ExpressionPart.PartType.EXPR, expr));
        return this;
    }

    public ExpressionDefinitionBuilder addMode(String mode)
    {
        parts.add(new ExpressionPart(ExpressionPart.PartType.MODE, mode));
        return this;
    }


}
