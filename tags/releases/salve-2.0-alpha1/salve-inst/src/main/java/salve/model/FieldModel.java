package salve.model;

public class FieldModel {
	private final int acces;
	private final String name;
	private final String desc;
	private final String signature;

	public FieldModel(int acces, String name, String desc, String signature) {
		this.acces = acces;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
	}

	public int getAcces() {
		return acces;
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}

}
