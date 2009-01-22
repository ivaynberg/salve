package salve.expr.inst;

import java.util.Set;

import salve.InstrumentationContext;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.inst.locator.MethodAnalyzer;
import salve.expr.inst.locator.Rule;
import salve.util.asm.MethodVisitorAdapter;

/** @deprecated */
@Deprecated
public class PeValidatorClassVisitor extends ClassAdapter
{
    private final InstrumentationContext ctx;
    private Type owner;
    private final Set<Rule> rules;

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
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions)
    {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv == null)
        {
            mv = new MethodVisitorAdapter();
        }
        return new MethodAnalyzer(mv, owner, rules, ctx.getLoader());
    }

}
