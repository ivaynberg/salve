package salve.contract.pe;

import java.util.Map;

public class TestPolicy implements Policy {

	public Accessor choose(Map<Accessor.Type, Accessor> accessors) {
		return accessors.values().iterator().next();
	}

}
