package salve.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassesUtil {
	private static final String CLASS_LOADER_REFLECT_CLASS_NAME = "java.lang.ClassLoader";
	private static final String DEFINE_CLASS_METHOD_NAME = "defineClass";
	private static final Class<?>[] DEFINE_CLASS_METHOD_PARAMS = new Class[] { String.class, byte[].class, int.class,
			int.class };

	private ClassesUtil() {

	}

	public static void checkClassNameArg(String className) {
		if (className == null) {
			throw new IllegalArgumentException("Argument `className` cannot be null");
		}
		if (className.trim().length() != className.length()) {
			throw new IllegalArgumentException("Argument `className` cannot contain white space");
		}
		if (className.length() == 0) {
			throw new IllegalArgumentException("Argument `className` cannot be empty");
		}
	}

	public static Class<?> loadClass(final String className, byte[] bytecode) {
		checkClassNameArg(className);
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}

		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> klass = loader.loadClass(CLASS_LOADER_REFLECT_CLASS_NAME);
			Method method = klass.getDeclaredMethod(DEFINE_CLASS_METHOD_NAME, DEFINE_CLASS_METHOD_PARAMS);

			method.setAccessible(true);
			try {
				Object[] args = new Object[] { className.replace("/", "."), bytecode, 0, bytecode.length };
				Class<?> clazz = (Class<?>) method.invoke(loader, args);
				return clazz;
			} finally {
				method.setAccessible(false);
			}

		} catch (InvocationTargetException e) {
			// TODO exception
			throw new RuntimeException(e);
		} catch (Exception e) {
			// TODO exception
			throw new RuntimeException(e);
		}
	}

}
