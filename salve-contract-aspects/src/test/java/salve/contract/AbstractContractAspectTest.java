package salve.contract;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;


public abstract class AbstractContractAspectTest extends TestCase
{
    private static final String PAD = "0000000000000000000000000000000000000000000000000000000000000000000000000000";

    protected <T> void executeArgumentTestHarness(Object bean, String methodBaseName, int count,
            T positiveValue, T negativeValue) throws Throwable
    {
        for (int stage = 0; stage < count; stage++)
        {
            final int permutations = (int)Math.pow(2, stage + 1);
            for (int permutation = 0; permutation < permutations; permutation++)
            {
                T[] values = getPermutationArray(stage + 1, permutation, positiveValue,
                        negativeValue);
                try
                {
                    try
                    {
                        if (methodBaseName == null)
                        {
                            findConstructorByArgCount(bean.getClass(), stage+1).newInstance(values);
                        }
                        else
                        {
                            findMethodByName(bean.getClass(), methodBaseName + stage).invoke(bean,
                                    values);
                        }
                    }
                    catch (InvocationTargetException e)
                    {
                        throw ((InvocationTargetException)e).getTargetException();
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Unknown exception thrown", e);
                    }

                    if (permutation < permutations - 1)
                    {
                        fail(String
                                .format(
                                        "Should have thrown an exception. Stage: %d, iteration: %d/%d",
                                        stage+1, permutation+1, permutations));
                    }
                }
                catch (IllegalArgumentException e)
                {
                    if (permutation == permutations - 1)
                    {
                        throw new RuntimeException(
                                "Last permutation should contain all positive values, but exception was thrown",
                                e);
                    }
                }
            }
        }
    }


    private Constructor< ? > findConstructorByArgCount(Class< ? > clazz, int argCount)
    {
        for (Constructor< ? > constructor : clazz.getConstructors())
        {
            if (constructor.getParameterTypes().length == argCount)
            {
                return constructor;
            }
        }
        return null;
    }


    private static Method findMethodByName(Class< ? > clazz, String methodName)
    {
        for (Method method : clazz.getMethods())
        {
            if (method.getName().equals(methodName))
            {
                return method;
            }
        }
        return null;
    }


    private static interface Callback<T>
    {
        void execute(T[] v, int iteration, int total);
    }

    private static String getPermutationString(int size, int permutation)
    {
        String bin = Integer.toBinaryString(permutation);
        bin = PAD.substring(0, size - bin.length()) + bin;
        return bin;
    }

    private static <T> T[] getPermutationArray(int size, int permutation, T positive, T negative)
    {
        T[] values = (T[])Array.newInstance(positive.getClass(), size);
        String bin = getPermutationString(size, permutation);
        for (int j = 0; j < bin.length(); j++)
        {
            values[j] = (bin.charAt(j) == '1') ? positive : negative;
        }
        return values;
    }
}
