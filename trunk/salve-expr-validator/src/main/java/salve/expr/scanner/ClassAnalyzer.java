package salve.expr.scanner;

import java.util.Set;

import salve.BytecodeLoader;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.util.asm.ClassVisitorAdapter;

public class ClassAnalyzer extends ClassAdapter
{

    private final Set<Rule> defs;
    private final BytecodeLoader loader;
    private final Errors errors;
    private Type owner;

    public ClassAnalyzer(Set<Rule> defs, BytecodeLoader loader, Errors errors)
    {
        this(new ClassVisitorAdapter(), defs, loader, errors);
    }

    public ClassAnalyzer(ClassVisitor cv, Set<Rule> defs, BytecodeLoader loader, Errors errors)
    {
        super(cv);
        this.defs = defs;
        this.loader = loader;
        this.errors = errors;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);
        owner = Type.getObjectType(name);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions)
    {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodAnalyzer(mv, owner, defs, loader, errors);
    }
}
