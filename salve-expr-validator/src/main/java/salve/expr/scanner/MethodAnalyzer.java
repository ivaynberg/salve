/**
 * 
 */
package salve.expr.scanner;

import java.util.List;
import java.util.Set;

import salve.BytecodeLoader;
import salve.CodeMarker;
import salve.InstrumentationException;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.checker.ExpressionChecker;

public class MethodAnalyzer extends RuleMatcher
{
    private final BytecodeLoader loader;

    public MethodAnalyzer(MethodVisitor mv, Type owner, Set<Rule> definitions, BytecodeLoader loader)
    {
        super(mv, owner, definitions);
        this.loader = loader;
    }

    @Override
    protected void onInvalid(Type target, Type container, List<Instruction> parts, CodeMarker marker)
    {
        throw new InstrumentationException("invalid instantiation", marker);
    }

    @Override
    protected void onMatch(Expression expr)
    {
        ExpressionChecker validator = new ExpressionChecker(loader);
        validator.validate(expr);
    }
}