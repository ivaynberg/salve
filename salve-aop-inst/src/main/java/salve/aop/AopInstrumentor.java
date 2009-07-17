package salve.aop;

import salve.CannotLoadBytecodeException;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.util.BytecodeLoadingClassWriter;

public class AopInstrumentor implements Instrumentor
{

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

        reader.accept(new ClassInstrumentor(writer, ctx.getModel()), ClassReader.EXPAND_FRAMES);
        bytecode = writer.toByteArray();

        return bytecode;

    }

}
