package salve.guice.model;

import salve.Dependency;

public class Injected {
	// tests lookup by type
	@Dependency
	private TestService testService;

	// tests lookup by type and annot
	@Dependency
	@Blue
	private TestService blueTestService;

	public TestService getBlueTestService() {
		return blueTestService;
	}

	public TestService getTestService() {
		return testService;
	}

}
