package salve.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ListMap<K, V> extends HashMap<K, List<V>> {
	public void add(K key, V value) {
		if (containsKey(key)) {
			get(key).add(value);
		} else {
			List<V> values = new ArrayList<V>(1);
			values.add(value);
			put(key, values);
		}
	}

	@Override
	public List<V> get(Object key) {
		List<V> list = super.get(key);
		if (list == null) {
			list = Collections.emptyList();
		}
		return list;
	}

}
