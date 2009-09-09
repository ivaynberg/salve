package salve.aop.inst;

import java.util.HashSet;
import java.util.Set;

import salve.CannotLoadBytecodeException;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.util.BytecodeLoadingClassWriter;

public class AopInstrumentor implements Instrumentor
{

    Set<AspectProvider> aspects = new HashSet<AspectProvider>();

    public AopInstrumentor()
    {
        aspects.add(new AnnotationAspectProvider());
    }

    public byte[] instrument(String className, InstrumentationContext ctx)
            throws InstrumentationException
    {
        byte[] bytecode = ctx.getLoader().loadBytecode(className);
        if (bytecode == null)
        {
            throw new CannotLoadBytecodeException(className);
        }

        ClassReader reader = new ClassReader(bytecode);

        ClassWriter writer = new BytecodeLoadingClassWriter(ClassWriter.COMPUTE_FRAMES, ctx
                .getLoader());

        reader.accept(newInstrumentor(ctx, writer), ClassReader.EXPAND_FRAMES);
        bytecode = writer.toByteArray();

        // TODO hacky: notify model that we have updated the class
        ctx.getModel().update(bytecode);
        return bytecode;

    }

    private ClassInstrumentor newInstrumentor(InstrumentationContext ctx, ClassWriter writer)
    {
        return new ClassInstrumentor(writer, getAspectProviders(), ctx);
    }

    public Set<AspectProvider> getAspectProviders()
    {
        return aspects;
    }
}
