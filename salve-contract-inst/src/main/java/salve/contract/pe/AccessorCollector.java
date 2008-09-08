package salve.contract.pe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.asmlib.ClassReader;
import salve.util.asm.AsmUtil;
import salve.util.asm.InvalidSignatureException;
import salve.util.asm.AsmUtil.Pair;

public class AccessorCollector {
	private static final Pattern listIndexPattern = Pattern.compile("[0-9]+");

	private final InstrumentationContext ctx;

	public AccessorCollector(InstrumentationContext ctx) {
		this.ctx = ctx;
	}

	public Map<Accessor.Type, Accessor> collect(final String className, String part, String mode, Accessor previous,
			PeDefinition def) {
		Map<Accessor.Type, Accessor> accessors = new HashMap<Accessor.Type, Accessor>();

		AccessorCollectorClassVisitor visitor = new AccessorCollectorClassVisitor(part, mode);

		for (EnhancedClassReader reader : new ClassHieararchy(ctx.getLoader(), className)) {
			if (reader.isMap()) {
				final Pair<String, String> sig;
				try {
					sig = AsmUtil.parseMapTypesFromSignature(previous.getSig());
				} catch (InvalidSignatureException e) {
					throw new InstrumentationException("Part: " + part + " of property expression: "
							+ def.getExpression() + " accesses a Map with an unsupported signature: "
							+ previous.getSig(), def.getMarker());
				}
				final String type = sig.getValue();
				final Accessor acc = new Accessor(Accessor.Type.MAP, part, "L" + type + ";", null);
				accessors.put(acc.getType(), acc);
			} else if (reader.isList()) {
				if (!listIndexPattern.matcher(part).matches()) {
					throw new InstrumentationException("Property expression: " + def.getExpression()
							+ " contains an invalid list index: " + part, def.getMarker());
				}
				final String type;
				try {
					type = AsmUtil.parseListTypeFromSignature(previous.getSig());
				} catch (InvalidSignatureException e) {
					throw new InstrumentationException("Part: " + part + " of property expression: "
							+ def.getExpression() + " accesses a List with an unsupported signature: "
							+ previous.getSig(), def.getMarker());

				}
				final Accessor acc = new Accessor(Accessor.Type.LIST, part, "L" + type + ";", null);
				accessors.put(acc.getType(), acc);
			}

			reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			for (Accessor accessor : visitor.getAccessors()) {
				accessors.put(accessor.getType(), accessor);
			}

		}

		return Collections.unmodifiableMap(accessors);
	}
}
