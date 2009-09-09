package salve.expr.scanner;


import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;

abstract class BytecodeFilter extends MethodAdapter
{

    public BytecodeFilter(MethodVisitor mv)
    {
        super(mv);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc)
    {
        onNoise();
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(int var, int increment)
    {
        onNoise();
        super.visitIincInsn(var, increment);
    }

    @Override
    public final void visitInsn(int opcode)
    {
        if (opcode == Opcodes.DUP)
        {
            onDup();
        }
        else
        {
            onNoise();
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand)
    {
        onNoise();
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label)
    {
        onNoise();
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst)
    {
        if (cst instanceof String)
        {
            onLoadString((String)cst);
        }
        else if (cst instanceof Type)
        {
            onLoadType((Type)cst);
        }
        else
        {
            onNoise();
        }
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
    {
        onNoise();
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc)
    {
        if (opcode == Opcodes.INVOKESPECIAL && "<init>".equals(name))
        {
            onInvokeConstructor(Type.getObjectType(owner));
        }
        else
        {
            onNoise();
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims)
    {
        onNoise();
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
    {
        onNoise();
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitTypeInsn(int opcode, String type)
    {
        if (opcode == Opcodes.NEW)
        {
            onNew(Type.getObjectType(type));
        }
        else
        {
            onNoise();
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(int opcode, int var)
    {
        if (opcode == Opcodes.ALOAD && var == 0)
        {
            onLoadThis();
        }
        else
        {
            onNoise();
        }
        super.visitVarInsn(opcode, var);
    }


    protected abstract void onLoadThis();

    protected abstract void onNew(Type type);

    protected abstract void onDup();

    protected abstract void onLoadString(String str);

    protected abstract void onLoadType(Type type);

    protected abstract void onInvokeConstructor(Type type);

    protected abstract void onNoise();

}