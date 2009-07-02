package salve.config.model;

public class Address {
	private String street;
	private String city;
	private String zip;

	public Address(String street, String city, String zip) {
		this.street = street;
		this.city = city;
		this.zip = zip;
	}

	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getZip() {
		return zip;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

}
