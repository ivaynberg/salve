package salve.expr.validator;

import org.junit.Test;

import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassReader;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.expr.inst.Constants;
import salve.expr.inst.ExpressionUseLocator;
import salve.expr.inst.PeDefinition;
import salve.loader.ClassLoaderLoader;
import salve.util.asm.ClassVisitorAdapter;

public class UsagesTest
{
    @Test
    public void test()
    {
        ClassLoaderLoader loader = new ClassLoaderLoader(getClass().getClassLoader());
        ClassReader reader = new ClassReader(loader.loadBytecode("salve/expr/validator/Usages"));
        reader.accept(new Locator(), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
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
