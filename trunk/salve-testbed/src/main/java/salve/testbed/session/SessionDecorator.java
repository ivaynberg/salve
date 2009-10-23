package salve.testbed.session;

import java.io.Serializable;
import java.sql.Connection;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.hibernate.stat.SessionStatistics;

public abstract class SessionDecorator implements Session
{
    protected abstract Session getDelegate();

    public Transaction beginTransaction() throws HibernateException
    {
        return getDelegate().beginTransaction();
    }

    public void cancelQuery() throws HibernateException
    {
        getDelegate().cancelQuery();
    }

    public void clear()
    {
        getDelegate().clear();
    }

    public Connection close() throws HibernateException
    {
        return getDelegate().close();
    }

    public Connection connection() throws HibernateException
    {
        return getDelegate().connection();
    }

    public boolean contains(Object arg0)
    {
        return getDelegate().contains(arg0);
    }

    public Criteria createCriteria(Class arg0, String arg1)
    {
        return getDelegate().createCriteria(arg0, arg1);
    }

    public Criteria createCriteria(Class arg0)
    {
        return getDelegate().createCriteria(arg0);
    }

    public Criteria createCriteria(String arg0, String arg1)
    {
        return getDelegate().createCriteria(arg0, arg1);
    }

    public Criteria createCriteria(String arg0)
    {
        return getDelegate().createCriteria(arg0);
    }

    public Query createFilter(Object arg0, String arg1) throws HibernateException
    {
        return getDelegate().createFilter(arg0, arg1);
    }

    public Query createQuery(String arg0) throws HibernateException
    {
        return getDelegate().createQuery(arg0);
    }

    public SQLQuery createSQLQuery(String arg0) throws HibernateException
    {
        return getDelegate().createSQLQuery(arg0);
    }

    public void delete(Object arg0) throws HibernateException
    {
        getDelegate().delete(arg0);
    }

    public void delete(String arg0, Object arg1) throws HibernateException
    {
        getDelegate().delete(arg0, arg1);
    }

    public void disableFilter(String arg0)
    {
        getDelegate().disableFilter(arg0);
    }

    public Connection disconnect() throws HibernateException
    {
        return getDelegate().disconnect();
    }

    public void doWork(Work arg0) throws HibernateException
    {
        getDelegate().doWork(arg0);
    }

    public Filter enableFilter(String arg0)
    {
        return getDelegate().enableFilter(arg0);
    }

    public void evict(Object arg0) throws HibernateException
    {
        getDelegate().evict(arg0);
    }

    public void flush() throws HibernateException
    {
        getDelegate().flush();
    }

    public Object get(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException
    {
        return getDelegate().get(arg0, arg1, arg2);
    }

    public Object get(Class arg0, Serializable arg1) throws HibernateException
    {
        return getDelegate().get(arg0, arg1);
    }

    public Object get(String arg0, Serializable arg1, LockMode arg2) throws HibernateException
    {
        return getDelegate().get(arg0, arg1, arg2);
    }

    public Object get(String arg0, Serializable arg1) throws HibernateException
    {
        return getDelegate().get(arg0, arg1);
    }

    public CacheMode getCacheMode()
    {
        return getDelegate().getCacheMode();
    }

    public LockMode getCurrentLockMode(Object arg0) throws HibernateException
    {
        return getDelegate().getCurrentLockMode(arg0);
    }

    public Filter getEnabledFilter(String arg0)
    {
        return getDelegate().getEnabledFilter(arg0);
    }

    public EntityMode getEntityMode()
    {
        return getDelegate().getEntityMode();
    }

    public String getEntityName(Object arg0) throws HibernateException
    {
        return getDelegate().getEntityName(arg0);
    }

    public FlushMode getFlushMode()
    {
        return getDelegate().getFlushMode();
    }

    public Serializable getIdentifier(Object arg0) throws HibernateException
    {
        return getDelegate().getIdentifier(arg0);
    }

    public Query getNamedQuery(String arg0) throws HibernateException
    {
        return getDelegate().getNamedQuery(arg0);
    }

    public Session getSession(EntityMode arg0)
    {
        return getDelegate().getSession(arg0);
    }

    public SessionFactory getSessionFactory()
    {
        return getDelegate().getSessionFactory();
    }

    public SessionStatistics getStatistics()
    {
        return getDelegate().getStatistics();
    }

    public Transaction getTransaction()
    {
        return getDelegate().getTransaction();
    }

    public boolean isConnected()
    {
        return getDelegate().isConnected();
    }

    public boolean isDirty() throws HibernateException
    {
        return getDelegate().isDirty();
    }

    public boolean isOpen()
    {
        return getDelegate().isOpen();
    }

    public Object load(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException
    {
        return getDelegate().load(arg0, arg1, arg2);
    }

    public Object load(Class arg0, Serializable arg1) throws HibernateException
    {
        return getDelegate().load(arg0, arg1);
    }

    public void load(Object arg0, Serializable arg1) throws HibernateException
    {
        getDelegate().load(arg0, arg1);
    }

    public Object load(String arg0, Serializable arg1, LockMode arg2) throws HibernateException
    {
        return getDelegate().load(arg0, arg1, arg2);
    }

    public Object load(String arg0, Serializable arg1) throws HibernateException
    {
        return getDelegate().load(arg0, arg1);
    }

    public void lock(Object arg0, LockMode arg1) throws HibernateException
    {
        getDelegate().lock(arg0, arg1);
    }

    public void lock(String arg0, Object arg1, LockMode arg2) throws HibernateException
    {
        getDelegate().lock(arg0, arg1, arg2);
    }

    public Object merge(Object arg0) throws HibernateException
    {
        return getDelegate().merge(arg0);
    }

    public Object merge(String arg0, Object arg1) throws HibernateException
    {
        return getDelegate().merge(arg0, arg1);
    }

    public void persist(Object arg0) throws HibernateException
    {
        getDelegate().persist(arg0);
    }

    public void persist(String arg0, Object arg1) throws HibernateException
    {
        getDelegate().persist(arg0, arg1);
    }

    public void reconnect() throws HibernateException
    {
        getDelegate().reconnect();
    }

    public void reconnect(Connection arg0) throws HibernateException
    {
        getDelegate().reconnect(arg0);
    }

    public void refresh(Object arg0, LockMode arg1) throws HibernateException
    {
        getDelegate().refresh(arg0, arg1);
    }

    public void refresh(Object arg0) throws HibernateException
    {
        getDelegate().refresh(arg0);
    }

    public void replicate(Object arg0, ReplicationMode arg1) throws HibernateException
    {
        getDelegate().replicate(arg0, arg1);
    }

    public void replicate(String arg0, Object arg1, ReplicationMode arg2) throws HibernateException
    {
        getDelegate().replicate(arg0, arg1, arg2);
    }

    public Serializable save(Object arg0) throws HibernateException
    {
        return getDelegate().save(arg0);
    }

    public Serializable save(String arg0, Object arg1) throws HibernateException
    {
        return getDelegate().save(arg0, arg1);
    }

    public void saveOrUpdate(Object arg0) throws HibernateException
    {
        getDelegate().saveOrUpdate(arg0);
    }

    public void saveOrUpdate(String arg0, Object arg1) throws HibernateException
    {
        getDelegate().saveOrUpdate(arg0, arg1);
    }

    public void setCacheMode(CacheMode arg0)
    {
        getDelegate().setCacheMode(arg0);
    }

    public void setFlushMode(FlushMode arg0)
    {
        getDelegate().setFlushMode(arg0);
    }

    public void setReadOnly(Object arg0, boolean arg1)
    {
        getDelegate().setReadOnly(arg0, arg1);
    }

    public void update(Object arg0) throws HibernateException
    {
        getDelegate().update(arg0);
    }

    public void update(String arg0, Object arg1) throws HibernateException
    {
        getDelegate().update(arg0, arg1);
    }
    
    
}
