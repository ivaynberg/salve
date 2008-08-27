package salve.contract.pe;

public class Address {
	private String street;
	private City city;

	public City getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public void setStreet(String street) {
		this.street = street;
	}

}
