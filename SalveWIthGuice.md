
```
package test.salve;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import salve.depend.Dependency;
import salve.depend.DependencyLibrary;
import salve.depend.guice.GuiceBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class SalveGuiceTest
{
 static interface TestService
 {
   String getValue();
 }

 static class TestServiceImpl implements TestService
 {
   @Inject
   private @Named("value")
   String value;

   public String getValue()
   {
     return value;
   }
 }

 static class Entity
 {
   @Dependency
   private TestService testService;

   public String getFoo()
   {
     return testService.getValue();
   }
 }

 static class TestModule extends AbstractModule
 {
   @Override
   protected void configure()
   {
     bind(TestService.class).to(TestServiceImpl.class).in
(Scopes.SINGLETON);
     bind(String.class).annotatedWith(Names.named("value")).toInstance
("foo");
   }
 }

 @BeforeClass
 public void setUp()
 {
   Injector injector = Guice.createInjector(new TestModule());
   DependencyLibrary.addLocator(new GuiceBeanLocator(injector));
 }

 @Test
 public void test()
 {
   Entity entity = new Entity();
   Assert.assertEquals(entity.getFoo(), "foo");
 }
}
```