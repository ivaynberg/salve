package salve.loader;

import java.util.concurrent.ConcurrentHashMap;

import salve.Instrumentor;
import salve.monitor.NoopMonitor;
import salve.util.ClassesUtil;

public class BytecodePool extends CompoundLoader {

	private static final byte[] NOT_FOUND = new byte[] {};

	private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<String, byte[]>();

	public BytecodePool addLoaderFor(ClassLoader loader) {
		addLoader(new ClassLoaderLoader(loader));
		return this;
	}

	public BytecodePool addLoaderFor(String name, byte[] bytecode) {
		addLoader(new MemoryLoader(name, bytecode));
		return this;
	}

	public byte[] instrumentIntoBytecode(String className, Instrumentor inst) throws Exception {
		byte[] bytecode = inst.instrument(className, this, NoopMonitor.INSTANCE);
		save(className, bytecode);
		return bytecode;
	}

	public Class<?> instrumentIntoClass(String className, Instrumentor inst) throws Exception {
		instrumentIntoBytecode(className, inst);
		return loadClass(className);
	}

	@Override public byte[] loadBytecode(String className) {
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
	public Class<?> loadClass(final String className) throws ClassNotFoundException {
		ClassesUtil.checkClassNameArg(className);
		byte[] bytecode = loadBytecode(className);
		if (bytecode == null) {
			throw new ClassNotFoundException(className);
		}
		return loadClass(className, bytecode);
	}

	public Class<?> loadClass(final String className, byte[] bytecode) {
		return ClassesUtil.loadClass(className, bytecode);
	}

	public void save(String className, byte[] bytecode) {
		ClassesUtil.checkClassNameArg(className);
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}
		cache.put(className, bytecode);
	}

}
