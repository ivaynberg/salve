package salve.expr.inst.locator;

import java.util.List;
import java.util.Set;

import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;

public class RuleMatcher extends InstructionRecorder
{
    private final Type owner;
    final Set<Rule> definitions;

    public RuleMatcher(MethodVisitor mv, Type owner, Set<Rule> definitions)
    {
        super(mv, definitions);
        this.owner = owner;
        this.definitions = definitions;
    }

    

  
    private void onMatch(Expression expr)
    {
        System.out.println(expr);
    }



    @Override
    protected void onInstructionRecorded(Type container, List<Instruction> parts)
    {
        // try to match the instruction
        for (Rule definition : definitions)
        {
            if (definition.getContainer().equals(container))
            {
                if (parts.size() >= definition.getParts().length)
                {
                    boolean matched = true;
                    Type target = null;
                    String expr = null;
                    String mode = null;

                    for (int i = 0; i < definition.getParts().length; i++)
                    {
                        final int instOffset = parts.size() - 1 - i;
                        final Instruction inst = parts.get(instOffset);
                        final int partOffset = definition.getParts().length - 1 - i;
                        final Part part = definition.getParts()[partOffset];

                        if (!inst.matches(part))
                        {
                            matched = false;
                            break;
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
                    if (matched)
                    {
                        onMatch(new Expression(target, expr, (mode == null) ? "r" : mode));
                        break;
                    }
                }
            }
        }
        
    }

}
