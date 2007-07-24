package salve.asm.loader;

import java.util.ArrayList;
import java.util.List;

import salve.asm.BytecodeLoader;

public class CompoundLoader implements BytecodeLoader {
	private final List<BytecodeLoader> delegates = new ArrayList<BytecodeLoader>();

	public CompoundLoader addLoader(BytecodeLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException(
					"Argument `loader` cannot be null");
		}
		delegates.add(loader);
		return this;
	}

	public byte[] loadBytecode(String className) {
		for (BytecodeLoader loader : delegates) {
			byte[] bytecode = loader.loadBytecode(className);
			if (bytecode != null) {
				return bytecode;
			}
		}
		return null;
	}
}
