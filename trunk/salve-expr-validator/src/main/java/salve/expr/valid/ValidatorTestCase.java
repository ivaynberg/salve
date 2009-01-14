package salve.expr.valid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import salve.InstrumentationContext;
import salve.Scope;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassVisitor;
import salve.expr.inst.Constants;
import salve.expr.inst.PeValidatorClassVisitor;
import salve.loader.BytecodePool;
import salve.loader.ClassLoaderLoader;
import salve.loader.FilePathLoader;
import salve.monitor.NoopMonitor;
import salve.util.asm.ClassVisitorAdapter;

public class ValidatorTestCase
{
    private final boolean DEBUG = false;

    private static final String CLASSES = "target" + File.separatorChar + "classes";

    protected Scope getScope() {
        return Scope.ALL;
    }
    
    protected File[] getClassFileRoots()
    {
        File target = new File(new File("").getAbsolutePath());

        File classes = null;

        String basedir = System.getProperty("basedir");
        if (basedir != null && basedir.length() > 0)
        {
            classes = new File(basedir);
        }
        else
        {
            classes = new File(target, CLASSES);
        }

        if (classes.exists() && classes.isDirectory())
        {
            target = classes;
        }

        return new File[] { target };
    }

    @Test
    public final void test()
    {
        File[] classFileRoots = getClassFileRoots();
        ClassFileVisitor visitor = new ClassFileValidator(classFileRoots);
        for (File classFileRoot : classFileRoots)
        {
            visit(classFileRoot, visitor);
        }

    }

    private void visit(File directory, ClassFileVisitor visitor)
    {
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                visit(file, visitor);
            }
            else
            {
                if (file.getName().endsWith(".class"))
                {
                    if (DEBUG)
                    {
                        System.err.println("visiting: " + file.getAbsolutePath());
                    }
                    visitor.visit(file);
                }
            }
        }
    }

    private static class ClassFileValidator implements ClassFileVisitor
    {
        private final InstrumentationContext context;

        public ClassFileValidator(File... outputFolders)
        {
            BytecodePool pool = new BytecodePool(Scope.ALL);
            for (File outputFolder : outputFolders)
            {
                pool.addLoader(new FilePathLoader(outputFolder));
            }
            pool.addLoader(new ClassLoaderLoader(getClass().getClassLoader()));

            context = new InstrumentationContext(pool, NoopMonitor.INSTANCE, Scope.ALL);
        }

        public void visit(File classFile)
        {
            FileInputStream in;
            try
            {
                in = new FileInputStream(classFile);

                try
                {
                    ClassReader reader = new ClassReader(in);
                    ClassVisitor visitor = new PeValidatorClassVisitor(Constants.PE,
                            Constants.PE_INIT, context, new ClassVisitorAdapter());

                    reader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
                finally
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException("Could not read bytecode from: " +
                                classFile.getAbsolutePath());
                    }
                }
            }
            catch (IOException e1)
            {
                throw new RuntimeException("Could not read bytecode from: " +
                        classFile.getAbsolutePath());
            }
        }
    }

    private static interface ClassFileVisitor
    {
        void visit(File classFile);
    }

}
