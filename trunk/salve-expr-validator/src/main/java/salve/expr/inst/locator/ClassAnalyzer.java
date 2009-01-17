package salve.expr.inst.locator;

import java.util.List;
import java.util.Set;

import org.junit.Assert;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.inst.PeDefinition;
import salve.expr.inst.PeValidator;
import salve.util.asm.ClassVisitorAdapter;

public class ClassAnalyzer extends ClassAdapter
{

    private final Set<Rule> defs;
    private final BytecodeLoader loader;
    private Type owner;

    public ClassAnalyzer(Set<Rule> defs, BytecodeLoader loader)
    {
        this(new ClassVisitorAdapter(), defs, loader);
    }

    public ClassAnalyzer(ClassVisitor cv, Set<Rule> defs, BytecodeLoader loader)
    {
        super(cv);
        this.defs = defs;
        this.loader = loader;
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
        return new MethodAnalyzer(mv, owner, defs, loader);
    }

    private static class MethodAnalyzer extends RuleMatcher
    {
        private final BytecodeLoader loader;

        public MethodAnalyzer(MethodVisitor mv, Type owner, Set<Rule> definitions,
                BytecodeLoader loader)
        {
            super(mv, owner, definitions);
            this.loader = loader;
        }

        @Override
        protected void onInvalid(Type target, Type container, List<Instruction> parts)
        {
            Assert.fail("invalid instantiation");
        }

        @Override
        protected void onMatch(Expression expr)
        {
            PeValidator validator = new PeValidator(loader);

            PeDefinition def = new PeDefinition();
            def.setExpression(expr.getExpression());
            def.setMode(expr.getMode());
            def.setType(expr.getType());
            try
            {
                validator.validate(def);
            }
            catch (InstrumentationException e)
            {
                Assert.fail("invalid expression: " + expr);
            }
        }
    }


}
