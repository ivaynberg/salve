package salve.aop.inst;

import org.junit.Test;

import salve.aop.Traced;

public class AopInstrumentorTest extends AbstractAopInstrumentorTestSupport
{

    public static class Bean
    {
        @Traced
        public void hello()
        {
            System.out.println("hello");
// RuntimeException e=new RuntimeException("TRACE");
// e.printStackTrace();
        }

        @Traced
        public void hello(String str, int num)
        {
            System.out.println("hello str=" + str + " num=" + num);
        }

        @Traced
        public void hello(int[] nums)
        {
            System.out.println("hello nums.length=" + nums.length);
        }

    }

    @Test
    public void test() throws Exception
    {
        Bean bean = create("Bean");
        bean.hello();
        bean.hello("string", 7);
        bean.hello(new int[] { 0, 1 });
    }

}
