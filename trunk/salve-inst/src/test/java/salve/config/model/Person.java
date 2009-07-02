package salve.config.model;

public class Person {
	private String name;
	private Address address;

	public Person() {
	}

	public Person(String name) {
		this.name = name;
	}

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
