/**
 * 
 */
package salve.expr.scanner;

import java.util.List;
import java.util.Set;

import salve.BytecodeLoader;
import salve.CodeMarker;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.checker.CheckerException;
import salve.expr.checker.ExpressionChecker;

public class MethodAnalyzer extends RuleMatcher
{
    private final BytecodeLoader loader;
    private final Errors errors;

    public MethodAnalyzer(MethodVisitor mv, Type owner, Set<Rule> definitions,
            BytecodeLoader loader, Errors errors)
    {
        super(mv, owner, definitions);
        this.loader = loader;
        this.errors = errors;
    }

    @Override
    protected void onInvalid(Type container, List<Instruction> parts, CodeMarker marker)
    {
        errors.report(null, new StringBuilder("Invalid instantion of: ").append(container.getClassName())
            .append(" at: ").append(marker).toString());
    }

    @Override
    protected void onMatch(Expression expr)
    {
        ExpressionChecker validator = new ExpressionChecker(loader);
        try
        {
            validator.validate(expr);
        }
        catch (CheckerException e)
        {
            errors.report(e.getError());
        }
    }
}