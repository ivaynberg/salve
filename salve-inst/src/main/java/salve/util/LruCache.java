package salve.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Quick and dirty lru cache
 * 
 * @author ivaynberg
 * 
 */
public class LruCache extends LinkedHashMap {
	protected int maxsize;

	public LruCache(int maxsize) {
		super(maxsize * 4 / 3 + 1, 0.75f, true);
		this.maxsize = maxsize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > maxsize;
	}
}