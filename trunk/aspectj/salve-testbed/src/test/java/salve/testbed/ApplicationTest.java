package salve.testbed;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

public class ApplicationTest
{
    private ConfigurableApplicationContext ctx;

    @Before
    public void setup()
    {
        ctx = new ClassPathXmlApplicationContext("META-INF/application.xml");
        ctx.registerShutdownHook();
    }

    @After
    public void teardown()
    {
        ctx.close();
    }

    @Test
    @Transactional
    public void test()
    {
        assertTrue(Article.findAll().isEmpty());
        Article article = new Article("test");
        article.setContent("test");
        article.save();
        assertTrue(Article.findAll().size() == 1);
    }
}
