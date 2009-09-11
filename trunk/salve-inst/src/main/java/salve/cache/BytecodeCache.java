package salve.cache;

import salve.Bytecode;

public interface BytecodeCache {
	public static final BytecodeCache NOOP_CACHE = new BytecodeCache() {

		public Bytecode get(String className) {
			return null;
		}

		public void put(Bytecode bytecode) {
		}

	};

	Bytecode get(String className);

	void put(Bytecode bytecode);
}
