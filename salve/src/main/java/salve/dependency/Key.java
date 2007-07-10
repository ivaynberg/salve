package salve.dependency;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import salve.dependency.impl.DependencyConstants;


public class Key implements Serializable {
	private final Class<?> dependencyClass;

	private final Class<?> injectedClass;

	private final String injectedFieldName;

	private final Annotation[] injectedFieldAnnots;

	public Key(Class<?> dependencyClass, Class<?> injectedClass,
			String injectedFieldName) {
		// we have to pass in dependency class because the field can be removed
		// so reflection is not always available

		this.injectedClass = injectedClass;
		this.injectedFieldName = injectedFieldName;
		this.dependencyClass = dependencyClass;
		Field field;
		try {
			field = injectedClass.getField(DependencyConstants
					.keyFieldName(injectedFieldName));
			injectedFieldAnnots = field.getAnnotations();
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
		} else if (obj instanceof Key) {
			Key other = (Key) obj;
			return injectedClass.equals(other.injectedClass)
					&& injectedFieldName.equals(other.injectedFieldName);
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotationOfType(Class<T> clazz) {
		for (Annotation annot : injectedFieldAnnots) {
			if (clazz.isAssignableFrom(annot.annotationType())) {
				return (T) annot;
			}
		}
		return null;
	}

	public Class getDependencyClass() {
		return dependencyClass;
	}

	public Class<?> getInjectedClass() {
		return injectedClass;
	}

	public Annotation[] getInjectedFieldAnnots() {
		return injectedFieldAnnots;
	}

	public String getInjectedFieldName() {
		return injectedFieldName;
	}

	@Override
	public int hashCode() {
		return injectedClass.hashCode() + 37 * injectedFieldName.hashCode();
	}

	@Override
	public String toString() {
		return String.format(
				"[%s dependencyType=%s injectedType=%s injectedFieldName=%s ",
				Key.class.getName(), dependencyClass, injectedClass,
				injectedFieldName);
	}

	private Object writeReplace() throws ObjectStreamException {
		return new SerializedKey(injectedClass, injectedFieldName);
	}

	private static class SerializedKey implements Serializable {
		private final String injectedClassName;
		private final String injectedFieldName;

		public SerializedKey(Class injectedClass, String injectedFieldName) {
			super();
			this.injectedClassName = injectedClass.getName();
			this.injectedFieldName = injectedFieldName;
		}

		private Object readResolve() throws ObjectStreamException {
			try {
				Class injectedClass = Class.forName(injectedClassName);
				Field field = injectedClass.getField(DependencyConstants
						.keyFieldName(injectedFieldName));

				final Key key = (Key) field.get(null);
				return key;
			} catch (Exception e) {
				throw new ObjectStreamException(getClass().getName()) {
				};
			}
		}
	}
}
