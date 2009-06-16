package salve.aop.inst;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import salve.aop.MethodAdvice;
import salve.aop.MethodInvocation;

public class AopInstrumentorTest extends AbstractAopInstrumentorTestSupport
{
// TODO inheritance - make sure overridden methods are also instrumented in super has annot

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "simple")
    public @interface Simple {

    }

    @MethodAdvice(instrumentorClass = AopInstrumentorTest.BeanAdvice.class, instrumentorMethod = "args")
    public @interface Args {

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

// @Traced
// public void hello(String str, int num)
// {
// System.out.println("hello str=" + str + " num=" + num);
// }

// @Traced
// public void hello(int[] nums)
// {
// System.out.println("hello nums.length=" + nums.length);
// }

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

// @Test
// public void shouldInstrumentMethodsWithArguments() throws Exception
// {
// Bean bean = create("Bean");
// bean.test1();
// assertTrue(bean.methodsCalled.contains("test1"));
// assertTrue(bean.aspectsCalled.contains("test1"));
// }

}
