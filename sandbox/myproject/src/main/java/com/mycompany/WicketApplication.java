package com.mycompany;

import org.apache.wicket.protocol.http.WebApplication;

import salve.depend.DependencyLibrary;
import salve.depend.guice.GuiceBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Application object for your web application. If you want to run this application without
 * deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
    /**
     * Constructor
     */
    public WicketApplication()
    {
    }

    /**
     * @see wicket.Application#getHomePage()
     */
    public Class getHomePage()
    {
        return HomePage.class;
    }

    @Override
    protected void init()
    {
        Module module = new AbstractModule()
        {

            @Override
            protected void configure()
            {
                bind(Service.class).toInstance(new ServiceImpl());
            }

        };

        Injector injector = Guice.createInjector(module);
        DependencyLibrary.addLocator(new GuiceBeanLocator(injector));
    }
}
