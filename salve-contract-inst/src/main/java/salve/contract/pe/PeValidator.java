package salve.contract.pe;

import java.util.Map;

import salve.InstrumentationContext;

public class PeValidator {
	private final InstrumentationContext ctx;

	public PeValidator(InstrumentationContext ctx) {
		this.ctx = ctx;
	}

	public void validate(PeDefinition def) {
		AccessorCollector collector = new AccessorCollector(ctx);
		Policy policy = new TestPolicy();
		String[] parts = def.getExpression().split("\\.");
		if (parts.length < 1) {
			throw new IllegalArgumentException("PE Expression: " + def.getExpression() + " must have at least one part");
		}
		String cn = def.getType().getInternalName();
		Accessor accessor = null;
		for (String part : parts) {
			Map<Accessor.Type, Accessor> accessors = collector.collect(cn, part, def.getMode(), accessor);
			if (accessors.isEmpty()) {
				throw new RuntimeException("Could not resolve expression part: " + part + " in class: " + cn);
			}
			accessor = policy.choose(accessors);
			cn = accessor.getReturnTypeName();
		}
	}
}
