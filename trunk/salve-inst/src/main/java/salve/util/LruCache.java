package salve.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Quick and dirty lru cache
 * 
 * @author ivaynberg
 * 
 * 
 * TODO copy paste from salve-inst, remove
 */
public class LruCache<K, V> {
	private final LinkedHashMap<K, V> cache;
	private int hitCount;
	private int missCount;

	/**
	 * Constructor
	 * 
	 * @param maxsize
	 *            maximum size of cache before lru eviction starts
	 */
	public LruCache(final int maxsize) {
		cache = new LinkedHashMap<K, V>(maxsize * 4 / 3 + 1, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > maxsize;
			};
		};
	}

	/**
	 * Gets cached value
	 * 
	 * @param key
	 * @return value or null
	 */
	public V get(K key) {
		V ret = cache.get(key);
		if (ret == null) {
			missCount++;
		} else {
			hitCount++;
		}
		return ret;
	}

	/**
	 * @return number of times {@link #get(Object)} returned a non-null value
	 */
	public int getHitCount() {
		return hitCount;
	}

	/**
	 * @return number of times {@link #get(Object)} returned a null value
	 */
	public int getMissCount() {
		return missCount;
	}

	/**
	 * Puts a value into cache
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		cache.put(key, value);
	}

	/**
	 * Resets hit and miss counters
	 */
	public void resetStatistics() {
		hitCount = 0;
		missCount = 0;
	}

	public int size() {
		return cache.size();
	}

}