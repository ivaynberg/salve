package salve.aop.inst;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;

import salve.aop.MethodAdvice;
import salve.aop.MethodInvocation;


public class AspectInheritanceTest extends AbstractAopInstrumentorTestSupport
{
    @Retention(RetentionPolicy.RUNTIME)
    @MethodAdvice(adviceClass = Advice.class, adviceMethod = "appendA")
    @Inherited
    public @interface AppendA {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @MethodAdvice(adviceClass = Advice.class, adviceMethod = "appendB")
    public @interface AppendB {

    }

    public static class Advice
    {
        public static Object appendA(MethodInvocation invocation) throws Throwable
        {
            return invocation.execute().toString() + "a";
        }

        public static Object appendB(MethodInvocation invocation) throws Throwable
        {
            return invocation.execute().toString() + "b";
        }

    }


    public static class Bean
    {
        @AppendA
        @AppendB
        public String echo(String str)
        {
            return str;
        }
    }

    public static class BeanSubclassDoesNotCallSuper extends Bean
    {
        @Override
        public String echo(String str)
        {
            return str.toUpperCase();
        }
    }

    public static class BeanSubclassDoesCallSuper extends Bean
    {
        @Override
        public String echo(String str)
        {
            return super.echo(str).toUpperCase();
        }
    }

    public static class DeepInheritanceDoesCallSuper extends BeanSubclassDoesCallSuper
    {
        @Override
        @AppendB
        public String echo(String str)
        {
            return super.echo(str);
        }
    }

    public static class DeepInheritanceDoesNotCallSuper extends BeanSubclassDoesCallSuper
    {
        @Override
        @AppendB
        public String echo(String str)
        {
            return str;
        }
    }


    @Before
    public void ensureClassesAreInstrumented() throws Exception
    {
        create("Bean");
        create("BeanSubclassDoesNotCallSuper");
        create("BeanSubclassDoesCallSuper");
    }


    @Test
    public void shouldExecuteBothAspectsOnOriginalClass() throws Exception
    {
        Bean bean = create("Bean");
        // both A and B should be applied because they are directly declared
        final String result = bean.echo("echo");
        assertTrue("echoab".equals(result) || "echoba".equals(result));
    }

    @Test
    public void shouldExecuteOnlyInheritedAspectsOnMethodOverrides() throws Exception
    {
        {
            Bean bean = create("BeanSubclassDoesNotCallSuper");
            // since neither A or B are directly declared only the aspects marked with @Inherited
            // should be applied - in this case only A
            final String result = bean.echo("echo");
            assertEquals("ECHOa", result);
        }
        {
            Bean bean = create("DeepInheritanceDoesNotCallSuper");
            // B is directly applied and A should be inherited
            final String result = bean.echo("echo");
            assertEquals("echoab", result);
        }
    }


}
