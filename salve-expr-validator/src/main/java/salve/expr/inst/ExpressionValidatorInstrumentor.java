package salve.expr.inst;

import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassVisitor;
import salve.util.asm.ClassVisitorAdapter;

/**
 * Salve instrumentor adapter for expression validator
 * @author igor.vaynberg
 *
 */
public class ExpressionValidatorInstrumentor implements Instrumentor
{

    public byte[] instrument(String className, InstrumentationContext context)
            throws InstrumentationException
    {
        byte[] bytecode = context.getLoader().loadBytecode(className);
        ClassReader reader = new ClassReader(bytecode);


        ClassVisitor visitor = new PeValidatorClassVisitor(Constants.PE, Constants.PE_INIT,
                context, new ClassVisitorAdapter());

        reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        return bytecode;
    }
}
