package salve.expr.checker;

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
        return pool.loadBytecode(clazz.getName().replace(".", "/"));
    }

    public BytecodeLoader getLoader()
    {
        return pool;
    }


}