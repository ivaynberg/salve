package salve.expr.inst.locator;

import java.util.List;
import java.util.Set;

import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;

public abstract class RuleMatcher extends InstructionRecorder
{
    private final Type owner;
    final Set<Rule> definitions;

    public RuleMatcher(MethodVisitor mv, Type owner, Set<Rule> definitions)
    {
        super(mv, definitions);
        this.owner = owner;
        this.definitions = definitions;
    }

    protected abstract void onMatch(Expression expr);

    private Expression match(Rule rule, List<Instruction> parts)
    {
        Type target = null;
        String expr = null;
        String mode = null;

        if (parts.size() >= rule.getParts().length)
        {

            for (int i = 0; i < rule.getParts().length; i++)
            {
                final int instOffset = parts.size() - 1 - i;
                final Instruction inst = parts.get(instOffset);
                final int partOffset = rule.getParts().length - 1 - i;
                final Part part = rule.getParts()[partOffset];

                if (!inst.matches(part))
                {
                    return null;
                }
                else
                {
                    switch (part)
                    {
                        case EXPR :
                            expr = inst.getData();
                            break;
                        case MODE :
                            mode = inst.getData();
                            break;
                        case TYPE :
                            target = Type.getObjectType(inst.getData());
                            break;
                        case THIS :
                            target = owner;
                            break;
                    }
                }
            }
            return new Expression(target, expr, (mode == null) ? "r" : mode);
        }
        return null;
    }

    @Override
    protected void onInstructionRecorded(Type container, List<Instruction> parts)
    {
        Expression match = null;
        // try to match the instruction
        for (Rule definition : definitions)
        {
            if (definition.getContainer().equals(container))
            {
                match = match(definition, parts);
                if (match != null)
                {
                    break;
                }
            }
        }
        if (match != null)
        {
            onMatch(match);
        }
        else
        {
            onInvalid(null, container, parts);
        }


    }

    protected abstract void onInvalid(Type target, Type container, List<Instruction> parts);
}
