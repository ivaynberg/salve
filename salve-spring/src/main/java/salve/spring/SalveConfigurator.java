package salve.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import salve.DependencyLibrary;

public class SalveConfigurator implements ApplicationContextAware {
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		DependencyLibrary.addLocator(new SpringBeanLocator(context));
	}

}
