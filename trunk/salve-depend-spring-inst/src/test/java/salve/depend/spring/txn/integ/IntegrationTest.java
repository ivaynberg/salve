package salve.depend.spring.txn.integ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import salve.Scope;
import salve.depend.spring.txn.TransactionalInstrumentor;
import salve.loader.BytecodePool;

public class IntegrationTest
{
    @BeforeClass
    public static void instrument() throws Exception
    {
        ClassLoader loader = IntegrationTest.class.getClassLoader();
        BytecodePool pool = new BytecodePool(Scope.ALL).addLoaderFor(loader);
        TransactionalInstrumentor inst = new TransactionalInstrumentor();
        pool.instrumentIntoClass("salve/depend/spring/txn/integ/Database", inst);
    }


    @Test
    public void test() throws Exception
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:salve/depend/spring/txn/integ/IntegrationTest.xml");

        Database database = (Database)ctx.getBean("database");

        assertEquals(0, database.count());

        database.insert("a");
        database.insert("b");

        assertEquals(2, database.count());
        Assert.assertArrayEquals(database.list().toArray(new String[0]), new String[] { "a", "b" });

        try
        {
            database.insertThatFails("c");
            fail();
        }
        catch (IllegalStateException e)
        {
            // no op
        }

        assertEquals(2, database.count());
        Assert.assertArrayEquals(new String[] { "a", "b" }, database.list().toArray(new String[0]));

        try
        {
            database.insertThatSucceedsWithException("c");
            fail();
        }
        catch (IllegalStateException e)
        {
            // no op
        }

        assertEquals(3, database.count());
        Assert.assertArrayEquals(new String[] { "a", "b", "c" }, database.list().toArray(
                new String[0]));

    }
}
