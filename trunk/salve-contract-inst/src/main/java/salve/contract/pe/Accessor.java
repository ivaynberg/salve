package salve.contract.pe;

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
				return salve.asmlib.Type.getType(desc).getInternalName();
			case GETTER:
				return salve.asmlib.Type.getReturnType(desc).getInternalName();
			case SETTER:
				return desc.substring(2, desc.length() - 3);
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
