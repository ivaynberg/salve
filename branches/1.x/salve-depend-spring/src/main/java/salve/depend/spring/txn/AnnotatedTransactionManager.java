package salve.depend.spring.txn;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * use this transaction manager type to support the famous @Transactional attribute on methods
 * <p/>
 * it will create the required AnnotationTransactionAttributeSource and TransactionInterceptor automatically.
 * <p/>
 * so there is no need to declare them in our spring xml or guice module or whatever.
 *
 * @author Peter Ertl
 * @see AnnotationTransactionAttributeSource
 * @see TransactionInterceptor
 */
public class AnnotatedTransactionManager implements TransactionManager
{
  // transaction wrapper (to get access to all this sneaky and hidden methods of spring)
  private final TransactionWrapper transaction;

  /**
   * create annotated transaction manager
   *
   * @param platformTransactionManager transaction manager instance
   */
  public AnnotatedTransactionManager(final PlatformTransactionManager platformTransactionManager)
  {
    if (platformTransactionManager == null)
      throw new IllegalArgumentException("platformTransactionManager can not be null");

    transaction = new TransactionWrapper(platformTransactionManager);
  }

  public Object start(final TransactionAttribute attr, final String txnName)
  {
    return transaction.start(attr, txnName);
  }

  public void finish(final Object txn)
  {
    transaction.finish(txn);
  }

  public void finish(final Throwable ex, final Object txn)
  {
    transaction.finish(txn, ex);
  }

  public void cleanup(final Object txn)
  {
    transaction.cleanup(txn);
  }

  /**
   * this wrapper is required to gain access to the transaction control methods of the spring class.
   * <p/>
   * these are initially protected so we override and delegate them to get access within the salve transaction manager.
   */
  private static final class TransactionWrapper extends TransactionInterceptor
  {
    private static final long serialVersionUID = 8678660935586442624L;

    private TransactionWrapper(final PlatformTransactionManager platformTransactionManager)
    {
      setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
      setTransactionManager(platformTransactionManager);
    }

    /**
     * this method delegates to the corresponding protected method which will clean up an transaction
     *
     * @param transactionInfo type-less transaction info (because the type itself is hidden in spring)
     */
    public void cleanup(final Object transactionInfo)
    {
      cleanupTransactionInfo((TransactionInfo) transactionInfo);
    }

    /**
     * this method delegates to the corresponding protected method which will start a transaction
     *
     * @param transactionAttribute transaction attribute
     * @param transactionName      transaction name
     * @return transaction info
     */
    public TransactionInfo start(final TransactionAttribute transactionAttribute, final String transactionName)
    {
      return createTransactionIfNecessary(transactionAttribute, transactionName);
    }

    /**
     * this method delegates to the corresponding protected method which will finish the transaction
     *
     * @param transactionInfo type-less transaction info (because the type itself is hidden in spring)
     */
    public void finish(final Object transactionInfo)
    {
      commitTransactionAfterReturning((TransactionAspectSupport.TransactionInfo) transactionInfo);
    }

    /**
     * this method delegates to the corresponding protected method which will finish the transaction on errors
     *
     * @param transactionInfo type-less transaction info (because the type itself is hidden in spring)
     * @param ex              error that interrupted the transaction
     */
    public void finish(final Object transactionInfo, final Throwable ex)
    {
      completeTransactionAfterThrowing((TransactionInfo) transactionInfo, ex);
    }
  }
}
