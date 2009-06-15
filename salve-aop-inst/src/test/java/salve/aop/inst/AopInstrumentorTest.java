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
//            RuntimeException e=new RuntimeException("TRACE");
//            e.printStackTrace();
        }
    }

    @Test
    public void test() throws Exception
    {
        Bean bean = create("Bean");
        bean.hello();
    }

}
