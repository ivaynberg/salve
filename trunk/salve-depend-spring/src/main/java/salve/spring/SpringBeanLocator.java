package salve.spring;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import salve.dependency.Key;
import salve.dependency.Locator;

public class SpringBeanLocator implements Locator {
	private final ApplicationContext context;

	public SpringBeanLocator(final ApplicationContext context) {
		super();
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public Object locate(Key key) {

		SpringBeanId id = null;
		// XXX refactor the loop into KeyUtils
		for (Annotation annot : key.getAnnotations()) {
			if (SpringBeanId.class.equals(annot.annotationType())) {
				id = (SpringBeanId) annot;
			}
		}

		if (id != null) {
			return context.getBean(id.value(), key.getType());
		}

		Map<String, Object> beans = context.getBeansOfType(key.getType());
		if (beans.size() == 1) {
			return beans.values().iterator().next();
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
