package salve.aop.inst.inline;

import java.io.PrintWriter;

import salve.AbstractInstrumentor;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationContext;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.asmlib.trace.TraceClassVisitor;
import salve.util.BytecodeLoadingClassWriter;

public class AopInstrumentor2 extends AbstractInstrumentor
{

    @Override
    protected byte[] internalInstrument(String className, InstrumentationContext ctx)
            throws Exception
    {
        byte[] bytecode = ctx.getLoader().loadBytecode(className);
        if (bytecode == null)
        {
            throw new CannotLoadBytecodeException(className);
        }

        ClassReader reader = new ClassReader(bytecode);

        ClassWriter writer = new BytecodeLoadingClassWriter(ClassWriter.COMPUTE_FRAMES, ctx
                .getLoader());

        reader.accept(new AopClassInstrumentor2(ctx, writer), ClassReader.EXPAND_FRAMES);
        bytecode = writer.toByteArray();

        // TODO hacky: notify model that we have updated the class
        ctx.getModel().update(bytecode);
        
        
        reader=new ClassReader(bytecode);
        reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
        
        
        return bytecode;
    }
}
