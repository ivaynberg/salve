## Overview ##
Salve uses a simple locator mechanism to lookup dependencies. The locator interface is shown below:

```
public interface salve.Locator {
  Object locate(salve.Key key);
}
```

Salve's `DependencyLibrary` object allows registration of multiple locators which are used in order of registration to lookup dependencies. The `Key` object provides meta information about the needed dependency such as its type, name of the field carrying the @Dependency annotation, as well as any additional annotations on that field.

## Spring Integration ##

Configuring Salve to use Spring for dependency lookups is trivial. Simply make sure salve-spring.jar is available on the classpath and add the following line into Spring's application context xml file:

```
<bean class="salve.depend.spring.SalveConfigurator" />
```

Lookups will be performed in the following fashion:
  1. If @SpringBeanId annotation is present on the field the specified bean id is used to lookup the bean from the application context
  1. A lookup of beans by annotated field's type is performed
    1. If no beans are found lookup is passed to the next locator in chain
    1. If only a single bean of the requested type is found it is used
    1. If more than one bean is found a match is tried on the bean-id and field-name
    1. If no bean-id/field-name match is found lookup is passed to the next locator in chain

### Notes ###
Any dependency lookup performed before the initialization of Spring's application context will not use Salve's spring locator and thus will most likely fail.

## Guice Integration ##
Make sure salve-guice.jar is in the classpath, and add the following line after you have constructed the Guice Injector:

```
DependencyLibrary.addLocator(new GuiceBeanLocator(injector));
```

For a more complete example see the [Salve With Guice](SalveWIthGuice.md) page.

### Notes ###
Because Guice uses the concept of implicit-bindings to instantiate types that have not been explicitly bound in modules it makes GuiceBeanLocator hard to chain, therefore it is recommended to be installed at the end of the chain.

## Custom Integration ##
Coming soon, for now here is the code to `SpringBeanLocator` which should give a good idea:

```

public class SpringBeanLocator implements Locator {
	private final ApplicationContext context;

	public SpringBeanLocator(final ApplicationContext context) {
		super();
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public Object locate(Key key) {
		SpringBeanId id = key.getAnnotationOfType(SpringBeanId.class);
		if (id != null) {
			return context.getBean(id.value(), key.getDependencyClass());
		}

		Map<String, Object> beans = context.getBeansOfType(key
				.getDependencyClass());
		if (beans.size() == 1) {
			return beans.values().iterator().next();
		} else if (beans.size() > 1) {
			Object bean = beans.get(key.getInjectedFieldName());
			return bean != null ? bean : null;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("[%s context=%s]", getClass().getName(), context
				.getDisplayName());
	}

}
```

Installation:

```
DependencyLibrary.addLocator(new SpringBeanLocator(applicationContext));
```

### Tips ###
It is a good idea to override `toString()` method of the locator implementation so Salve can provide better error messages.