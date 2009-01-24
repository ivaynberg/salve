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
import salve.expr.checker.PeDefinition;
import salve.expr.checker.PeValidator;

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
    protected void onMatch(Expression expr, CodeMarker marker)
    {
        PeValidator validator = new PeValidator(loader);

        PeDefinition def = new PeDefinition();
        def.setExpression(expr.getExpression());
        def.setMode(expr.getMode());
        def.setType(expr.getType());
        validator.validate(def);
    }
}