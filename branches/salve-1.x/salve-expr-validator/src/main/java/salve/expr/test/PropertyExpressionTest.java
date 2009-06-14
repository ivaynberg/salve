package salve.expr.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import salve.BytecodeLoader;
import salve.Scope;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassVisitor;
import salve.expr.scanner.ClassAnalyzer;
import salve.expr.scanner.Errors;
import salve.expr.scanner.Part;
import salve.expr.scanner.Rule;
import salve.loader.BytecodePool;
import salve.loader.ClassLoaderLoader;
import salve.loader.FilePathLoader;

/**
 * Base class for property expression validation test cases.
 * 
 * @author igor.vaynberg
 * 
 */
public class PropertyExpressionTest
{
    private final boolean DEBUG = false;

    private static final String CLASSES = "target" + File.separatorChar + "classes";

    /**
     * @return an array of directories that will be search for .class files
     */
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

    /**
     * @return rules that will be used to discover property expressions in bytecode
     */
    protected Set<Rule> getRules()
    {
        Set<Rule> defs = new HashSet<Rule>();
        // new Pe(Person.class, "address.street", "rw")
        defs.add(new Rule("salve/expr/Pe", Part.TYPE, Part.PATH, Part.MODE));
        // new Pe(Person.class, "address.street"); // use default mode
        defs.add(new Rule("salve/expr/Pe", Part.TYPE, Part.PATH));
        // new Pe(this, "name")
        defs.add(new Rule("salve/expr/Pe", Part.THIS, Part.PATH));

        return defs;
    }

    /**
     * Executes the test.
     */
    @Test
    public final void testExpressions()
    {
        File[] classFileRoots = getClassFileRoots();
        Errors errors = new Errors();
        ClassFileVisitor visitor = new ClassFileValidator(getRules(), errors, classFileRoots);
        for (File classFileRoot : classFileRoots)
        {
            visit(classFileRoot, visitor);
        }

        if (!errors.isEmpty())
        {
            Assert.fail(errors.toString());
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
        private final Errors errors;

        public ClassFileValidator(Set<Rule> defs, Errors errors, File... outputFolders)
        {
            this.defs = defs;
            this.errors = errors;
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
                    ClassVisitor visitor = new ClassAnalyzer(defs, loader, errors);
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
