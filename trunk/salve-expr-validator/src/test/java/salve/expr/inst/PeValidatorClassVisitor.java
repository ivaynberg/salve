package salve.expr.inst;

import java.util.Set;

import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.checker.Error;
import salve.expr.scanner.Errors;
import salve.expr.scanner.MethodAnalyzer;
import salve.expr.scanner.Rule;
import salve.util.asm.MethodVisitorAdapter;

/** @deprecated */
@Deprecated
public class PeValidatorClassVisitor extends ClassAdapter
{
    private final InstrumentationContext ctx;
    private Type owner;
    private final Set<Rule> rules;
    private final Errors errors = new Errors();

    public PeValidatorClassVisitor(Set<Rule> rules, InstrumentationContext ctx, ClassVisitor cv)
    {
        super(cv);
        this.ctx = ctx;
        this.rules = rules;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces)
    {
        owner = Type.getType("L" + name + ";");
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd()
    {
        super.visitEnd();
        if (!errors.isEmpty())
        {
            final Error error = errors.iterator().next();
            throw new InstrumentationException(error.toString());
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions)
    {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv == null)
        {
            mv = new MethodVisitorAdapter();
        }

        return new MethodAnalyzer(mv, owner, rules, ctx.getLoader(), errors);
    }

}
