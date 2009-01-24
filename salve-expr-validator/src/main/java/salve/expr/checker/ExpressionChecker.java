package salve.expr.checker;

import java.util.Map;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.expr.scanner.Expression;

public class ExpressionChecker
{
    private final BytecodeLoader loader;

    public ExpressionChecker(BytecodeLoader loader)
    {
        this.loader = loader;
    }

    public void validate(Expression expr)
    {
        AccessorCollector collector = new AccessorCollector(loader);
        Policy policy = new TestPolicy();
        String[] parts = expr.getPath().split("\\.");
        if (parts.length < 1)
        {
            throw new InstrumentationException("Property expression: " + expr.getPath() +
                    " must have at least one part", expr.getLocation());
        }
        String cn = expr.getType().getInternalName();
        Accessor accessor = null;
        for (String part : parts)
        {
            Map<Accessor.Type, Accessor> accessors = collector.collect(cn, part, expr.getMode(),
                accessor, expr);
            if (accessors.isEmpty())
            {
                throw new InstrumentationException("Could not resolve expression part: " + part +
                        " in class: " + cn + ", expression: " + expr.getPath(), expr
                    .getLocation());
            }
            accessor = policy.choose(accessors);
            cn = accessor.getReturnTypeName();
        }
    }
}
