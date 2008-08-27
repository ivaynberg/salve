package salve.contract.pe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import salve.InstrumentationContext;
import salve.asmlib.ClassReader;
import salve.util.asm.MapSignature;

public class AccessorCollector {
	private final InstrumentationContext ctx;

	public AccessorCollector(InstrumentationContext ctx) {
		this.ctx = ctx;
	}

	public Map<Accessor.Type, Accessor> collect(final String className, String part, String mode, Accessor previous) {
		Map<Accessor.Type, Accessor> accessors = new HashMap<Accessor.Type, Accessor>();

		AccessorCollectorClassVisitor visitor = new AccessorCollectorClassVisitor(part, mode);

		for (EnhancedClassReader reader : new ClassHieararchy(ctx.getLoader(), className)) {
			if (reader.isMap()) {
				MapSignature sig = new MapSignature();
				sig.parse(previous.getSig());
				final String type = sig.getValueTypeClassName();
				final Accessor acc = new Accessor(Accessor.Type.MAP, part, "L" + type + ";", null);
				accessors.put(acc.getType(), acc);
			} else if (reader.isList()) {
				throw new UnsupportedOperationException("lists are not yet supported");
			}

			reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			for (Accessor accessor : visitor.getAccessors()) {
				accessors.put(accessor.getType(), accessor);
			}

		}

		return Collections.unmodifiableMap(accessors);
	}
}
