package salve.loader;

import salve.BytecodeLoader;

public class MemoryLoader implements BytecodeLoader {
	private final String className;
	private final byte[] bytecode;

	public MemoryLoader(String className, byte[] bytecode) {
		super();
		if (className == null) {
			throw new IllegalArgumentException("Argument `className` cannot be null");
		}
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}
		this.bytecode = bytecode;
		this.className = className;
	}

	public byte[] loadBytecode(String className) {
		if (this.className.equals(className)) {
			return bytecode;
		}
		return null;
	}

}
