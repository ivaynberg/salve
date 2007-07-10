package salve.guice;

import java.lang.annotation.Annotation;

import salve.dependency.Key;
import salve.dependency.Locator;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;

/**
 * Salve locator that can connect dependency library to guice injector.
 * 
 * NOTE It is recommended to install this locator as the last in the chain
 * because of guice's implicit-binding concept which makes it difficult to
 * properly return null from {@link #locate(Key)} when no binding for specified
 * type has been created
 * 
 * @author ivaynberg
 * 
 */
public class GuiceBeanLocator implements Locator {
	private final Injector injector;

	public GuiceBeanLocator(final Injector injector) {
		super();
		this.injector = injector;
	}

	/**
	 * @see salve.dependency.Locator#locate(salve.dependency.Key)
	 */
	@SuppressWarnings("unchecked")
	public Object locate(Key key) {
		Annotation bindingAnnot = null;

		Annotation[] annots = key.getInjectedFieldAnnots();
		for (Annotation annot : annots) {
			if (annot.annotationType().getAnnotation(BindingAnnotation.class) != null) {
				bindingAnnot = annot;
				break;
			}
		}

		if (bindingAnnot == null) {
			return injector.getInstance(key.getDependencyClass());
		} else {
			return injector.getInstance(com.google.inject.Key.get(key
					.getDependencyClass(), bindingAnnot));
		}

		// XXX investigate properly returning null, see javadoc note
	}

	@Override
	public String toString() {
		return String.format("[%s injector=%s]", getClass().getName(), injector
				.toString());
	}

}
