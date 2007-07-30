package salve.dependency.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

import salve.dependency.Key;

public class KeyImpl implements Key {
	private static final long serialVersionUID = 1L;
	private static final Annotation[] EMPTY = new Annotation[0];
	private final Class<?> type;

	private final Annotation[] annots;

	public KeyImpl(Class<?> type) {
		super();
		this.type = type;
		annots = EMPTY;
	}

	public KeyImpl(Class<?> type, Annotation[] annots) {
		super();
		this.type = type;
		this.annots = annots;
	}

	public KeyImpl(Class<?> dependencyType, Class<?> keyOwner,
			String keyFieldName) {

		this.type = dependencyType;
		Field field;
		try {
			field = keyOwner.getDeclaredField(keyFieldName);
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
