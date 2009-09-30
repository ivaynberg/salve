package salve.testbed.session;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;

public class CurrentSessionProviderBean implements FactoryBean
{
    private final SessionFactory factory;

    public CurrentSessionProviderBean(SessionFactory factory)
    {
        this.factory = factory;
    }

    public Object getObject() throws Exception
    {
        return new SessionDecorator()
        {

            @Override
            protected Session getDelegate()
            {
                return factory.getCurrentSession();
            }

        };
    }

    public Class getObjectType()
    {
        return Session.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

}
