package salve.aop.inst;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import salve.aop.MethodAdvice;
import salve.aop.MethodInvocation;
import salve.aop.UndeclaredException;

public class AopInstrumentorTest extends AbstractAopInstrumentorTestSupport
{
// TODO inheritance - make sure overridden methods are also instrumented in super has annot

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "simple")
    public @interface Simple {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "args")
    public @interface Args {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "exceptions")
    public @interface Exceptions {

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
            return invocation.execute();
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
                    return;
                case 1 :
                    throw new IndexOutOfBoundsException();
                case 2 :
                    throw new IllegalArgumentException();
            }
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
    public void shouldHandleExceptions() throws Exception
    {
        Bean bean = create("Bean");

        bean.test3(0);

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
