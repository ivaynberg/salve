package salve.expr.scanner;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;

public abstract class InstructionRecorder extends BytecodeFilter
{

    private List<Instruction> parts;
    private Type container;
    private final Set<Rule> rules;

    public InstructionRecorder(MethodVisitor mv, Set<Rule> rules)
    {
        super(mv);
        this.rules = rules;
    }

    private void noise()
    {
        if (parts.size() == 0 || Instruction.Type.OTHER != parts.get(parts.size() - 1).getType())
        {
            parts.add(Instruction.OTHER);
        }
    }

    @Override
    protected void onDup()
    {
        if (container != null)
        {
            if (parts.size() == 0)
            {
                // allow dups right after NEW
            }
            else
            {
                noise();
            }
        }
    }

    @Override
    protected void onInvokeConstructor(Type type)
    {
        if (container != null)
        {
            if (container.equals(type))
            {
                onInstructionRecorded(container, parts);
                container = null;
                parts = null;
            }
            else
            {
                noise();
            }
        }

    }

    protected abstract void onInstructionRecorded(Type container, List<Instruction> parts);

    @Override
    protected void onLoadString(String str)
    {
        if (container != null)
        {
            parts.add(new Instruction(Instruction.Type.STRING, str));
        }
    }

    @Override
    protected void onLoadType(Type type)
    {
        if (container != null)
        {
            parts.add(new Instruction(Instruction.Type.TYPE, type.getInternalName()));
        }
    }

    @Override
    protected void onNew(Type type)
    {
        if (container == null)
        {
            for (Rule rule : rules)
            {
                if (rule.getContainer().equals(type))
                {

                    container = type;
                    parts = new ArrayList<Instruction>();
                    break;
                }
            }
        }
        else
        {
            noise();
        }

    }

    @Override
    protected void onNoise()
    {
        if (container != null)
        {
            noise();
        }
    }

    @Override
    protected void onLoadThis()
    {
        if (container != null)
        {
            parts.add(Instruction.THIS);
        }
    }

}