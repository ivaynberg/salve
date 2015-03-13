# Introduction #

Use salve for @Transactional support instead of spring.

# Details #

Salve provides declarative transaction support using @Transactional. It's similar to spring-aop but doesn't require runtime weaving, compilation with AspectJ or using proxies. Instead the bytecode of the affected class is modified by Salve to include calls to the platform transaction manager (begin / rollback / commit).

Here's a short sample:


```
package demo.salve.transactional;

import junit.framework.TestCase;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import salve.depend.DependencyLibrary;
import salve.depend.Key;
import salve.depend.Locator;
import salve.depend.spring.txn.AnnotatedTransactionManager;
import salve.depend.spring.txn.TransactionManager;

public class SalveTransactionalTest extends TestCase
{
  static
  {
    // create data source
    final DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl("jdbc:postgresql://localhost/mydb");
    dataSource.setUsername("test");
    dataSource.setPassword("test");

    // spring platform transaction manager
    final PlatformTransactionManager springTx = new DataSourceTransactionManager(dataSource);

    // salve transaction manager (which wraps spring platform transaction manager and applies to @Transactional annotation)
    final TransactionManager salveTx = new AnnotatedTransactionManager(springTx);

    // provide some very simple salve locator
    DependencyLibrary.addLocator(new Locator()
    {
      public Object locate(final Key key)
      {
        if (TransactionManager.class.equals(key.getType()))
          return salveTx;

        return null;
      }
    });
  }

  @Transactional(propagation = Propagation.SUPPORTS, timeout = 120, isolation = Isolation.SERIALIZABLE)
  public void testTransaction()
  {
    System.out.println("running inside a transaction now!");
    // do some nice transaction stuff in here
  }
}

```