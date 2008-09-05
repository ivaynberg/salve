package salve.contract.pe;

import java.util.Map;

import salve.InstrumentationContext;

public class PeValidator {
	private final InstrumentationContext ctx;

	public PeValidator(InstrumentationContext ctx) {
		this.ctx = ctx;
	}

	public void validate(String className, String expression, String mode) {

		AccessorCollector collector = new AccessorCollector(ctx);
		Policy policy = new TestPolicy();
		String[] parts = expression.split("\\.");
		if (parts.length < 1) {
			throw new IllegalArgumentException("PE Expression: " + expression + " must have at least one part");
		}
		String cn = className;
		Accessor accessor = null;
		for (String part : parts) {
			Map<Accessor.Type, Accessor> accessors = collector.collect(cn, part, mode, accessor);
			if (accessors.isEmpty()) {
				throw new RuntimeException("Could not resolve expression part: " + part + " in class: " + cn);
			}
			accessor = policy.choose(accessors);
			cn = accessor.getReturnTypeName();
		}
	}
}
