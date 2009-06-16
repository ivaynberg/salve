package salve.aop.inst;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import salve.aop.MethodAdvice;
import salve.aop.MethodInvocation;
import salve.aop.UndeclaredException;

//TODO inheritance - make sure overridden methods are also instrumented in super has annot

//TODO return values - partially complete

//TODO multiple aspects per class and per method
//TODO prevent double instrumentation - mark instrumented methods with a tag annot
//TODO instrumentor should check if the advice method exists
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
            Bean bean = (Bean)invocation.getThis();
            bean.aspectsCalled.add(invocation.getMethod().getName());
            return ((String)invocation.execute()).toUpperCase();
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

    @Test
    public void shouldNotHandleMethodsWithoutAnnotations() throws Exception
    {
        Bean bean = create("Bean");
        bean.test0();
        assertTrue(bean.methodsCalled.contains("test0"));
        assertTrue(bean.aspectsCalled.isEmpty());
    }

    @Test
    public void shouldHandleSimpleMethods() throws Exception
    {
        Bean bean = create("Bean");
        bean.test1();
        assertTrue(bean.methodsCalled.contains("test1"));
        assertTrue(bean.aspectsCalled.contains("test1"));
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
        assertTrue(bean.aspectsCalled.contains("test5"));
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
