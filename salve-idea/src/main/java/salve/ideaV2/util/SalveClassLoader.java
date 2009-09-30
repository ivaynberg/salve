package salve.ideaV2.util;

import salve.BytecodeLoader;
import salve.Bytecode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * disposable class loader to load instrumentors from project libraries
 *
 * @author Peter Ertl
 */
public final class SalveClassLoader extends ClassLoader
{
	private final BytecodeLoader bytecodeLoader;
	private final ConcurrentHashMap<String, Class<?>> definedClasses;

	public SalveClassLoader(final ClassLoader parent, final BytecodeLoader bytecodeLoader)
	{
		super(parent);
		this.bytecodeLoader = bytecodeLoader;
		definedClasses = new ConcurrentHashMap<String, Class<?>>();
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException
	{
		try
		{
			return super.findClass(name);
		}
		catch (ClassNotFoundException e)
		{
			if (!definedClasses.contains(name))
			{
				final Bytecode bytecode = bytecodeLoader.loadBytecode(name.replace('.', '/'));

				if (bytecode == null)
					throw new ClassNotFoundException(name);

				final byte[] bytes = bytecode.getBytes();

				definedClasses.putIfAbsent(name, defineClass(name, bytes, 0, bytes.length));
			}
			return definedClasses.get(name);
		}
	}
}
