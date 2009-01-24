package salve.expr.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import salve.BytecodeLoader;
import salve.Scope;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassVisitor;
import salve.expr.scanner.ClassAnalyzer;
import salve.expr.scanner.Part;
import salve.expr.scanner.Rule;
import salve.loader.BytecodePool;
import salve.loader.ClassLoaderLoader;
import salve.loader.FilePathLoader;

public abstract class ValidatorTestCase
{
    private final boolean DEBUG = false;

    private static final String CLASSES = "target" + File.separatorChar + "classes";

    protected Scope getScope()
    {
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

    protected Set<Rule> getRules()
    {
        Rule one = new Rule("salve/expr/PE", Part.TYPE, Part.EXPR, Part.MODE);
        Rule two = new Rule("salve/expr/PE", Part.TYPE, Part.EXPR);
        Rule three = new Rule("salve/expr/PE", Part.THIS, Part.EXPR);

        Set<Rule> defs = new HashSet<Rule>();
        defs.add(one);
        defs.add(two);
        defs.add(three);
        
        return defs;
    }

    public final void executeTest()
    {
        File[] classFileRoots = getClassFileRoots();
        ClassFileVisitor visitor = new ClassFileValidator(getRules(), classFileRoots);
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
        private final BytecodeLoader loader;
        private final Set<Rule> defs;

        public ClassFileValidator(Set<Rule> defs, File... outputFolders)
        {
            this.defs = defs;

            BytecodePool pool = new BytecodePool(Scope.ALL);
            for (File outputFolder : outputFolders)
            {
                pool.addLoader(new FilePathLoader(outputFolder));
            }
            pool.addLoader(new ClassLoaderLoader(getClass().getClassLoader()));

            loader = pool;
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
                    ClassVisitor visitor = new ClassAnalyzer(defs, loader);

                    reader.accept(visitor, ClassReader.SKIP_FRAMES);
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
