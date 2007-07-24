package salve.dependency;

import salve.dependency.Dependency;
import salve.dependency.InjectionStrategy;

public class TestBean {

	@Dependency
	private RedDependency red;

	@Dependency(strategy = InjectionStrategy.INJECT_FIELD)
	private BlueDependency blue;

	public void execute() {
		blue.method1();
		blue.method2();
	}

}
