package salve.expr.inst;

import java.util.HashSet;
import java.util.Set;

import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassVisitor;
import salve.expr.scanner.Part;
import salve.expr.scanner.Rule;
import salve.util.asm.ClassVisitorAdapter;

/**
 * Salve instrumentor adapter for expression validator
 * 
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

        Rule one = new Rule("salve/expr/Pe", Part.TYPE, Part.PATH, Part.MODE);
        Rule two = new Rule("salve/expr/Pe", Part.TYPE, Part.PATH);

        Set<Rule> rules = new HashSet<Rule>();
        rules.add(one);
        rules.add(two);

        ClassVisitor visitor = new PeValidatorClassVisitor(rules, context,
            new ClassVisitorAdapter());

        reader.accept(visitor, ClassReader.SKIP_FRAMES);

        return bytecode;
    }
}
