package salve.expr.checker;

import salve.Bytecode;
import salve.BytecodeLoader;
import salve.Scope;
import salve.loader.BytecodePool;


public abstract class AbstractTest
{
    private BytecodePool pool;

    public AbstractTest()
    {
        pool = new BytecodePool(Scope.ALL);
        pool.addLoaderFor(getClass().getClassLoader());
    }

    protected byte[] loadBytecode(Class< ? > clazz)
    {
        Bytecode bytecode = pool.loadBytecode(clazz.getName().replace(".", "/"));
        if (bytecode == null)
        {
            return null;
        }
        else
        {
            return bytecode.getBytes();
        }
    }

    public BytecodeLoader getLoader()
    {
        return pool;
    }


}