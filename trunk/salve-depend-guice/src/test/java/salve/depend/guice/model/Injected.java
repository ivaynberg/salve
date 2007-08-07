package salve.depend.guice.model;

import salve.depend.Dependency;

public class Injected {
	// tests lookup by type
	@Dependency
	private MockService testService;

	// tests lookup by type and annot
	@Dependency
	@Blue
	private MockService blueTestService;

	public MockService getBlueTestService() {
		return blueTestService;
	}

	public MockService getTestService() {
		return testService;
	}

}
