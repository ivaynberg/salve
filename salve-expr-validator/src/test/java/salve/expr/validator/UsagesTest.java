package salve.expr.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import salve.CodeMarker;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassReader;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.scanner.Expression;
import salve.expr.scanner.Instruction;
import salve.expr.scanner.Part;
import salve.expr.scanner.Rule;
import salve.expr.scanner.RuleMatcher;
import salve.loader.ClassLoaderLoader;
import salve.util.asm.ClassVisitorAdapter;

public class UsagesTest
{
    @Test
    public void test()
    {
        ClassLoaderLoader loader = new ClassLoaderLoader(getClass().getClassLoader());
        ClassReader reader = new ClassReader(loader.loadBytecode("salve/expr/validator/Usages"));

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
                protected void onInvalid( Type container, List<Instruction> parts,
                        CodeMarker marker)
                {
                    System.out.println("invalid");
                }

                @Override
                protected void onMatch(Expression expr)
                {
                    System.out.println("match: " + expr);
                }


            };
        }

    }


}
