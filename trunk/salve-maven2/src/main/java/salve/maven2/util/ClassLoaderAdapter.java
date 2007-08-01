package salve.maven2.util;

import salve.BytecodeLoader;

public class ClassLoaderAdapter extends ClassLoader {
	private final BytecodeLoader bl;
	private final ClassLoader delegate;

	public ClassLoaderAdapter(ClassLoader delegate, BytecodeLoader bytecodeLoader) {
		if (delegate == null) {
			throw new IllegalArgumentException("Argument `delegate` cannot be null");
		}
		if (bytecodeLoader == null) {
			throw new IllegalArgumentException("Argument `bytecodeLoader` cannot be null");
		}
		this.bl = bytecodeLoader;
		this.delegate = delegate;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return delegate.loadClass(name);
		} catch (ClassNotFoundException e) {
			return super.loadClass(name);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		final String className = name.replace(".", "/");
		byte[] bytecode = bl.loadBytecode(className);
		return defineClass(name, bytecode, 0, bytecode.length);
	}

}
