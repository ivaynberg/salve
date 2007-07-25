package salve.dependency;

public class Bean {

	@Dependency
	private RedDependency red;

	@Dependency(strategy = InjectionStrategy.INJECT_FIELD)
	private BlueDependency blue;

	private BlackDependency black;

	public BlackDependency getBlack() {
		return black;
	}

	public BlueDependency getBlue() {
		return blue;
	}

	public RedDependency getRed() {
		return red;
	}

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

	public void setBlack(BlackDependency black) {
		this.black = black;
	}

	public void setBlue(BlueDependency blue) {
		this.blue = blue;
	}

	public void setRed(RedDependency red) {
		this.red = red;
	}

}
