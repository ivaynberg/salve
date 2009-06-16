package salve.aop.inst;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import salve.Scope;
import salve.aop.AopInstrumentor;
import salve.aop.MethodAdvice;
import salve.aop.MethodInvocation;
import salve.aop.UndeclaredException;
import salve.loader.BytecodePool;


//FIXME inheritance - make sure overridden methods are also instrumented in super has annot?

//FIXME aspect filtering in the instrumentor to allow other instrumentors to extend the aop and yet not have double instrumentation

//TODO instrumentor should check if the advice method exists

//TODO return values - void methods should always get a null value from aspect
//TODO return values - more helpful exception then the classcast if value of wrong type is returned from the aspect

//XXX remove reflection lookups when creating invocation
//XXX instrumentation monitor integration
public class AopInstrumentorTest extends AbstractAopInstrumentorTestSupport
{
    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "simple")
    public @interface Simple {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "args")
    public @interface Args {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "exceptions")
    public @interface Exceptions {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "addOne")
    public @interface AddOne {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "uppercase")
    public @interface Uppercase {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "brackets")
    public @interface Brackets {

    }


    public static class BeanAdvice
    {
        public static Object simple(MethodInvocation invocation) throws Throwable
        {
            Bean bean = (Bean)invocation.getThis();
            bean.aspectsCalled.add(invocation.getMethod().getName());
            return invocation.execute();
        }

        public static Object args(MethodInvocation invocation) throws Throwable
        {
            Bean bean = (Bean)invocation.getThis();
            StringBuilder str = new StringBuilder();
            str.append(invocation.getMethod().getName());
            for (Object arg : invocation.getArguments())
            {
                str.append(":").append((arg.getClass().isArray()) ? Array.get(arg, 0) : arg);
            }
            bean.aspectsCalled.add(str.toString());
            return invocation.execute();
        }

        public static Object exceptions(MethodInvocation invocation) throws Throwable
        {
            int mode = (Integer)invocation.getArguments()[0];
            if (mode == 3)
            {
                throw new IllegalStateException();
            }
            else if (mode == 4)
            {
                throw new IOException();
            }

            Bean bean = (Bean)invocation.getThis();
            bean.aspectsCalled.add(invocation.getMethod().getName());
            return invocation.execute();
        }

        public static Object addOne(MethodInvocation invocation) throws Throwable
        {
            Bean bean = (Bean)invocation.getThis();
            bean.aspectsCalled.add(invocation.getMethod().getName());
            int result = (Integer)invocation.execute();
            return result + 1;
        }

        public static Object uppercase(MethodInvocation invocation) throws Throwable
        {
            return ((String)invocation.execute()).toUpperCase();
        }

        public static Object brackets(MethodInvocation invocation) throws Throwable
        {
            return "[" + invocation.execute() + "]";
        }

    }


    public static class Bean
    {
        private List<String> methodsCalled = new ArrayList<String>();
        private List<String> aspectsCalled = new ArrayList<String>();

        public void test0()
        {
            methodsCalled.add("test0");
        }

        @Simple
        public void test1()
        {
            methodsCalled.add("test1");
        }

        @Args
        public void test2(String object, int primitive, Object[] objectArray, long[] primitiveArray)
        {
            methodsCalled.add("test2:" + object + ":" + primitive + ":" + objectArray[0] + ":" +
                    primitiveArray[0]);
        }

        @Exceptions
        public void test3(int mode) throws IndexOutOfBoundsException
        {
            switch (mode)
            {
                case 0 :
                    methodsCalled.add("test3");
                    return;
                case 1 :
                    throw new IndexOutOfBoundsException();
                case 2 :
                    throw new IllegalArgumentException();
            }
        }

        @AddOne
        public int test4(int val)
        {
            methodsCalled.add("test4");
            return val;
        }

        @Uppercase
        public String test5(String input)
        {
            methodsCalled.add("test5");
            return input;
        }
    }

    public static class Bean2
    {
        @Uppercase
        @Brackets
        public String test6(String input)
        {
            return input;
        }

        @Brackets
        public String test7(@Uppercase String input)
        {
            return input;
        }

        @Uppercase
        @Brackets
        public Object test8(@Uppercase @Brackets String string)
        {
            return string;
        }

    }

    public static class Bean3
    {
        @Uppercase
        @Brackets
        public String test9(String input)
        {
            return input;
        }
    }

    @Test
    public void shouldNotHandleMethodsWithoutAnnotations() throws Exception
    {
        Bean bean = create("Bean");
        bean.test0();
        assertTrue(bean.methodsCalled.contains("test0"));
        assertTrue(bean.aspectsCalled.isEmpty());
    }

    @Test
    public void shouldHandleAnnotatedMethods() throws Exception
    {
        Bean bean = create("Bean");
        bean.test1();
        assertTrue(bean.methodsCalled.contains("test1"));
        assertTrue(bean.aspectsCalled.contains("test1"));
    }

    @Test
    public void shouldHandleAnnotatedArguments() throws Exception
    {
        Bean2 bean = create("Bean2");
        assertEquals("[HELLO]", bean.test7("hello"));
    }


    @Test
    public void shouldHandleArguments() throws Exception
    {
        Bean bean = create("Bean");
        bean.test2("a", 1, new Object[] { "b" }, new long[] { 2 });
        assertTrue(bean.methodsCalled.contains("test2:a:1:b:2"));
        assertTrue(bean.aspectsCalled.contains("test2:a:1:b:2"));
    }

    @Test
    public void shouldHandleMultipleInstrumentors() throws Exception
    {
        Bean2 bean = create("Bean2");
        assertEquals("[HELLO]", bean.test6("hello"));
    }

    @Test
    public void shouldHandleDoubleInstrumentation() throws Exception
    {
        final String beanName = "Bean3";
        final String cn = getClass().getName().replace(".", "/") + "$" + beanName;
        BytecodePool pool = new BytecodePool(Scope.ALL).addLoaderFor(getClass().getClassLoader());

        // double instrument the class
        pool.instrumentIntoBytecode(cn, new AopInstrumentor());
        pool.instrumentIntoBytecode(cn, new AopInstrumentor());

        Class< ? > clazz = pool.loadClass(cn);

        Bean3 bean = (Bean3)clazz.newInstance();
        assertEquals("[HELLO]", bean.test9("hello"));
    }

    @Test
    public void shouldHandleDuplicateInstrumentors() throws Exception
    {
        Bean2 bean = create("Bean2");
        assertEquals("[HELLO]", bean.test8("hello"));
    }

    @Test
    public void shouldHandlePrimitiveReturnTypes() throws Exception
    {
        Bean bean = create("Bean");
        assertEquals(2, bean.test4(1));
        assertTrue(bean.methodsCalled.contains("test4"));
        assertTrue(bean.aspectsCalled.contains("test4"));
    }

    @Test
    public void shouldHandleNonPrimitiveReturnTypes() throws Exception
    {
        Bean bean = create("Bean");
        assertEquals("HELLO", bean.test5("hello"));
        assertTrue(bean.methodsCalled.contains("test5"));
    }


    @Test
    public void shouldHandleExceptions() throws Exception
    {
        Bean bean = create("Bean");

        bean.test3(0);
        assertTrue(bean.methodsCalled.contains("test3"));
        assertTrue(bean.aspectsCalled.contains("test3"));

        try
        {
            bean.test3(1);
            fail("expected " + IndexOutOfBoundsException.class.getName());
        }
        catch (IndexOutOfBoundsException e)
        {
            // expected
        }

        try
        {
            bean.test3(2);
            fail("expected " + IllegalArgumentException.class.getName());
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        try
        {
            bean.test3(3);
            fail("expected " + IllegalStateException.class.getName());
        }
        catch (IllegalStateException e)
        {
            // expected
        }

        try
        {
            bean.test3(4);
            fail("expected " + UndeclaredException.class.getName());
        }
        catch (UndeclaredException e)
        {
            assertTrue(e.getCause().getClass().equals(IOException.class));
        }
    }
}
