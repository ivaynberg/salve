package salve.asm.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class BytecodePool extends CompoundLoader {

	private static final byte[] NOT_FOUND = new byte[] {};
	private static final String CLASS_LOADER_REFLECT_CLASS_NAME = "java.lang.ClassLoader";

	private static final String DEFINE_CLASS_METHOD_NAME = "defineClass";

	// TODO user a soft ref map
	private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<String, byte[]>();

	@Override
	public byte[] loadBytecode(String className) {
		byte[] bytecode = cache.get(className);
		if (bytecode == null) {
			bytecode = super.loadBytecode(className);
			if (bytecode == null) {
				bytecode = NOT_FOUND;
			}
			cache.put(className, bytecode);
		}

		if (bytecode == NOT_FOUND) {
			return null;
		} else {
			return bytecode;
		}
	}

	/**
	 * Loads class
	 * 
	 * @param className
	 *            the name of the class
	 * @return the class
	 * @throws ClassNotFoundException
	 */
	public Class loadClass(final String className)
			throws ClassNotFoundException {

		byte[] bytecode = loadBytecode(className);
		if (bytecode == null) {
			throw new ClassNotFoundException(className);
		}
		return loadClass(className, bytecode);
	}

	public Class loadClass(final String className, byte[] bytecode) {
		// TODO checkargs
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class klass = loader.loadClass(CLASS_LOADER_REFLECT_CLASS_NAME);
			Method method = klass.getDeclaredMethod(DEFINE_CLASS_METHOD_NAME,
					new Class[] { String.class, byte[].class, int.class,
							int.class });

			// TODO: what if we don't have rights to set this method to
			// accessible on this specific CL? Load it in System CL?
			method.setAccessible(true);
			Object[] args = new Object[] { className.replace("/", "."),
					bytecode, new Integer(0), new Integer(bytecode.length) };
			Class clazz = (Class) method.invoke(loader, args);
			method.setAccessible(false);
			return clazz;
		} catch (InvocationTargetException e) {
			// TODO exception
			throw new RuntimeException(e);
		} catch (Exception e) {
			// TODO exception
			throw new RuntimeException(e);
		}
	}

	public void save(String className, byte[] bytecode) {
		// TODO checkargs
		cache.put(className, bytecode);
	}

}
