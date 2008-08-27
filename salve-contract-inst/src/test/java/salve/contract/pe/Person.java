package salve.contract.pe;

public class Person {
	private String name;
	private Address address;

	public Address getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setName(String name) {
		this.name = name;
	}

}
