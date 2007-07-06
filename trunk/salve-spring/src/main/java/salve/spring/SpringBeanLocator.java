package salve.spring;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import salve.Key;
import salve.Locator;

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
