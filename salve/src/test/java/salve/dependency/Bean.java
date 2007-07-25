package salve.dependency;

public class Bean {

	@Dependency
	private RedDependency red;

	@Dependency(strategy = InjectionStrategy.INJECT_FIELD)
	private BlueDependency blue;

	public void run() {
		blue.method1();
		blue.method2();
	}

}
