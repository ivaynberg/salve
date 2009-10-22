package salve.cache;

import salve.Bytecode;

public class NoopBytecodeCache implements BytecodeCache {

	public Bytecode get(String className) {
		return null;
	}

	public void put(Bytecode bytecode) {
	}

}
