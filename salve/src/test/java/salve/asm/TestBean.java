package salve.asm;

import salve.dependency.Dependency;

public class TestBean {

	@Dependency
	private RedDependency dependency;

	public int doit() {
		int a = 5;
		int b = 2;
		a = a + b;
		return a;
	}

	public RedDependency getDependency() {
		return dependency;
	}
}
