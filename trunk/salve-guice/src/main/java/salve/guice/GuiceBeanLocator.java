package salve.guice;

import java.lang.annotation.Annotation;

import salve.Key;
import salve.Locator;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;

public class GuiceBeanLocator implements Locator {
	private final Injector injector;

	public GuiceBeanLocator(final Injector injector) {
		super();
		this.injector = injector;
	}

	@SuppressWarnings("unchecked")
	public Object locate(Key key) {
		Annotation binding = null;

		Annotation[] annots = key.getInjectedFieldAnnots();
		for (Annotation annot : annots) {
			if (annot.getClass().getAnnotation(BindingAnnotation.class) != null) {
				binding = annot;
				break;
			}
		}

		if (binding == null) {
			return injector.getInstance(key.getDependencyClass());
		} else {
			return injector.getInstance(com.google.inject.Key.get(key
					.getDependencyClass(), binding));
		}

	}

	@Override
	public String toString() {
		return String.format("[%s injector=%s]", getClass().getName(), injector
				.toString());
	}

}
