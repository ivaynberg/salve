package salve.guice.model;

public class TestService {
	private final String name;

	public TestService() {
		this(TestService.class.getName());
	}

	public TestService(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
