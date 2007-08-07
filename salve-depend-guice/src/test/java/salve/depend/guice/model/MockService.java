package salve.depend.guice.model;

public class MockService {
	private final String name;

	public MockService() {
		this(MockService.class.getName());
	}

	public MockService(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
