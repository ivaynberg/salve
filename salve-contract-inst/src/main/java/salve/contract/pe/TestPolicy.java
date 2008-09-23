package salve.contract.pe;

import java.util.Map;

public class TestPolicy implements Policy {

	public Accessor choose(Map<Accessor.Type, Accessor> accessors) {
		Accessor getter = accessors.get(Accessor.Type.GETTER);
		if (getter != null) {
			return getter;
		} else {
			return accessors.values().iterator().next();
		}
	}
}
