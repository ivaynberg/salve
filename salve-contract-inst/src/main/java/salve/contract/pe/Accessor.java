package salve.contract.pe;

import salve.asmlib.Method;
import salve.util.asm.AsmUtil;

public class Accessor {
	public static enum Type {
		FIELD, GETTER, SETTER, MAP, LIST
	}

	private final Type type;
	private final String name;
	private final String desc;
	private final String sig;

	public Accessor(Type type, String name, String desc, String sig) {
		this.type = type;
		this.name = name;
		this.desc = desc;
		this.sig = sig;
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public String getReturnTypeName() {

		switch (type) {
			case FIELD:
			case MAP:
			case LIST:
				if (AsmUtil.isPrimitive(salve.asmlib.Type.getType(desc))) {
					return desc;
				} else {
					return salve.asmlib.Type.getType(desc).getInternalName();
				}
			case GETTER:
				// getter sigs are ()return_type
				if (AsmUtil.isPrimitive(salve.asmlib.Type.getType(desc.substring(2)))) {
					return desc.substring(2);
				} else {
					return salve.asmlib.Type.getReturnType(desc).getInternalName();
				}
			case SETTER:
				final Method method = new Method(name, desc);
				final String cn = method.getArgumentTypes()[0].getInternalName();
				return cn;
			default:
				throw new IllegalStateException("Unhandled accessor type: " + type);
		}

	}

	public String getSig() {
		return sig;
	}

	public Type getType() {
		return type;
	}
}
