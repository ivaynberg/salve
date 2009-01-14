package salve.expr.inst;

import java.util.Map;

import salve.InstrumentationContext;
import salve.InstrumentationException;

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
			throw new InstrumentationException("Property expression: " + def.getExpression()
					+ " must have at least one part", def.getMarker());
		}
		String cn = def.getType().getInternalName();
		Accessor accessor = null;
		for (String part : parts) {
			Map<Accessor.Type, Accessor> accessors = collector.collect(cn, part, def.getMode(), accessor, def);
			if (accessors.isEmpty()) {
				throw new InstrumentationException("Could not resolve expression part: " + part + " in class: " + cn
						+ ", expression: " + def.getExpression(), def.getMarker());
			}
			accessor = policy.choose(accessors);
			cn = accessor.getReturnTypeName();
		}
	}
}
