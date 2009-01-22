package salve.expr.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import salve.CodeMarker;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassReader;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.inst.Constants;
import salve.expr.inst.ExpressionUseLocator;
import salve.expr.inst.PeDefinition;
import salve.expr.inst.locator.Expression;
import salve.expr.inst.locator.Instruction;
import salve.expr.inst.locator.Part;
import salve.expr.inst.locator.Rule;
import salve.expr.inst.locator.RuleMatcher;
import salve.loader.ClassLoaderLoader;
import salve.util.asm.ClassVisitorAdapter;

@Ignore
public class UsagesTest
{
    @Test
    public void test()
    {
        ClassLoaderLoader loader = new ClassLoaderLoader(getClass().getClassLoader());
        ClassReader reader = new ClassReader(loader.loadBytecode("salve/expr/validator/Usages"));
        reader.accept(new Locator(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);


        Rule one = new Rule("salve/expr/validator/PeModel", Part.TYPE, Part.EXPR, Part.MODE);
        Rule two = new Rule("salve/expr/validator/PeModel", Part.THIS, Part.EXPR, Part.MODE);
        Rule three = new Rule("salve/expr/validator/PeModel", Part.THIS, Part.EXPR);

        Set<Rule> defs = new HashSet<Rule>();
        defs.add(one);
        defs.add(two);
        defs.add(three);
        reader.accept(new NewLocator(defs), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    }

    private static class NewLocator extends ClassAdapter
    {
        private final Set<Rule> defs;
        private Type owner;

        public NewLocator(Set<Rule> defs)
        {
            super(new ClassVisitorAdapter());
            this.defs = defs;
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
            return new RuleMatcher(mv, owner, defs)
            {

                @Override
                protected void onInvalid(Type target, Type container, List<Instruction> parts,
                        CodeMarker marker)
                {
                    System.out.println("invalid");
                }

                @Override
                protected void onMatch(Expression expr, CodeMarker marker)
                {
                    System.out.println("match: " + expr);
                }


            };
        }

    }


    private static class Locator extends ClassAdapter
    {
        private String owner;

        public Locator()
        {
            super(new ClassVisitorAdapter());
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces)
        {
            super.visit(version, access, name, signature, superName, interfaces);
            this.owner = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                String[] exceptions)
        {
            MethodVisitor orig = super.visitMethod(access, name, desc, signature, exceptions);
            return new ExpressionUseLocator(Constants.PE, Constants.PE_INIT, Type
                .getObjectType(owner), orig)
            {

                @Override
                protected void validatePeInstantiation(PeDefinition data)
                {
                    System.out.println(data);

                }

            };
        }


    }
}
