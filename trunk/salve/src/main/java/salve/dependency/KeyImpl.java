package salve.dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

import salve.dependency.impl.DependencyConstants;

public class KeyImpl implements Key {
	private static final long serialVersionUID = 1L;

	private final Class<?> type;

	private final Annotation[] annots;

	public KeyImpl(Class<?> dependencyClass, Class<?> injectedClass,
			String injectedFieldName) {
		// we have to pass in dependency class because the field can be removed
		// so reflection is not always available

		this.type = dependencyClass;
		Field field;
		try {
			field = injectedClass.getField(DependencyConstants
					.keyFieldName(injectedFieldName));
			annots = field.getAnnotations();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof KeyImpl) {
			KeyImpl other = (KeyImpl) obj;
			return type.equals(other.type)
					&& Arrays.equals(annots, other.annots);
		} else {
			return false;
		}
	}

	public Annotation[] getAnnotations() {
		return annots;
	}

	public Class<?> getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return type.hashCode() + 37 * annots.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[%s type=%s annots=%s]", getClass().getName(),
				type.getName(), annots);
	}
}
