package salve.testbed;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ApplicationTest extends TestCase
{
    @Test
    public void test()
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/application.xml");

        PlatformTransactionManager ptm = (PlatformTransactionManager)ctx.getBean("data.transactionManager");
        TransactionStatus txn=ptm.getTransaction(new DefaultTransactionDefinition());

        assertTrue(Article.findAll().isEmpty());
        Article article = new Article("test");
        article.setContent("test");
        article.save();
        assertTrue(Article.findAll().size() == 1);
        
        txn.setRollbackOnly();
        ptm.rollback(txn);
        
    }
}
