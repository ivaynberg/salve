package salve;

/**
 * Bytecode representation
 * 
 * @author igor.vaynberg
 * 
 */
public class Bytecode {
	private final String name;
	private final byte[] bytes;
	private final BytecodeLoader loader;

	public Bytecode() {
		name = null;
		bytes = null;
		loader = null;
	}

	public Bytecode(String name, byte[] bytes, BytecodeLoader loader) {
		if (bytes == null) {
			throw new IllegalArgumentException("Cannot create bytecode with no bytes");
		}
		this.name = name;
		this.bytes = bytes;
		this.loader = loader;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public BytecodeLoader getLoader() {
		return loader;
	}

	public String getName() {
		return name;
	}

}
