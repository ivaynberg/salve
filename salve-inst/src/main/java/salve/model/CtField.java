package salve.model;

public class CtField {
	private final int acces;
	private final String name;
	private final String desc;
	private final String signature;

	public CtField(int acces, String name, String desc, String signature) {
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
