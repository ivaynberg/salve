package salve.dependency;

public class Bean {

	@Dependency
	private RedDependency red;

	@Dependency(strategy = InjectionStrategy.INJECT_FIELD)
	private BlueDependency blue;

	public void method1() {
		blue.method1();
		blue.method2();
		red.method1();
		red.method2();
	}

	public void method2() {
		blue.method1();
		blue.method2();
		red.method1();
		red.method2();
	}

}
