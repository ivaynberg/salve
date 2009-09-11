package salve.cache;

import salve.Bytecode;
import salve.util.LruCache;

public class LruBytecodeCache implements BytecodeCache {
	private final LruCache<String, Bytecode> cache;

	public LruBytecodeCache(int size) {
		cache = new LruCache<String, Bytecode>(size);
	}

	public Bytecode get(String className) {
		return cache.get(className);
	}

	public void put(Bytecode bytecode) {
		cache.put(bytecode.getName(), bytecode);
	}

}
