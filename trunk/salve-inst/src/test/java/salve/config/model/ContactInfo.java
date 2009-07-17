package salve.config.model;

public class ContactInfo extends Address {
	private String phone;

	public ContactInfo(String street, String city, String zip) {
		super(street, city, zip);
	}

	public String getPhone() {
		return phone;
	}

	@Override
	public String getStreet() {
		return super.getStreet();
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
