package salve.expr.checker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.asmlib.ClassReader;
import salve.expr.scanner.Expression;
import salve.expr.util.ClassHieararchy;
import salve.expr.util.EnhancedClassReader;
import salve.util.asm.AsmUtil;
import salve.util.asm.InvalidSignatureException;
import salve.util.asm.AsmUtil.Pair;

public class AccessorCollector
{
    private static final Pattern listIndexPattern = Pattern.compile("[0-9]+");

    private final BytecodeLoader loader;

    public AccessorCollector(BytecodeLoader loader)
    {
        this.loader = loader;
    }

    public Map<Accessor.Type, Accessor> collect(final String className, String part, String mode,
            Accessor previous, Expression expr)
    {
        Map<Accessor.Type, Accessor> accessors = new HashMap<Accessor.Type, Accessor>();

        AccessorCollectorClassVisitor visitor = new AccessorCollectorClassVisitor(part, mode);

        for (EnhancedClassReader reader : new ClassHieararchy(loader, className))
        {
            if (reader.isMap())
            {
                final Pair<String, String> sig;
                try
                {
                    sig = AsmUtil.parseMapTypesFromSignature(previous.getSig());
                }
                catch (InvalidSignatureException e)
                {
                    throw new InstrumentationException("Part: " + part +
                            " of property expression: " + expr.getPath() +
                            " accesses a Map with an unsupported signature: " + previous.getSig(),
                        expr.getLocation());
                }
                final String type = sig.getValue();
                final Accessor acc = new Accessor(Accessor.Type.MAP, part, "L" + type + ";", null);
                accessors.put(acc.getType(), acc);
            }
            else if (reader.isList())
            {
                if (!listIndexPattern.matcher(part).matches())
                {
                    throw new InstrumentationException("Property expression: " +
                            expr.getPath() + " contains an invalid list index: " + part, expr
                        .getLocation());
                }
                final String type;
                try
                {
                    type = AsmUtil.parseListTypeFromSignature(previous.getSig());
                }
                catch (InvalidSignatureException e)
                {
                    throw new InstrumentationException("Part: " + part +
                            " of property expression: " + expr.getPath() +
                            " accesses a List with an unsupported signature: " + previous.getSig(),
                        expr.getLocation());

                }
                final Accessor acc = new Accessor(Accessor.Type.LIST, part, "L" + type + ";", null);
                accessors.put(acc.getType(), acc);
            }

            reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG |
                    ClassReader.SKIP_FRAMES);
            for (Accessor accessor : visitor.getAccessors())
            {
                accessors.put(accessor.getType(), accessor);
            }

        }

        return Collections.unmodifiableMap(accessors);
    }
}
