package salve.dependency;

public class Bean {

	// force this bean to have a clinit
	@SuppressWarnings("unused")
	public static final long FORCE_CLINIT = System.currentTimeMillis();

	private static BlackDependency staticBlack;

	@Square
	@Dependency
	@Circle
	private RedDependency red;

	@Dependency(strategy = InjectionStrategy.INJECT_FIELD)
	private BlueDependency blue;

	@Circle
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
		Object tmp = null;
		if (tmp == null) {
			tmp = red.method2();
		}
		red.method1();

	}

	public void methodInner() {
		InnerBean bean = new InnerBean();
		bean.method();

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

	public static BlackDependency getStaticBlack() {
		return staticBlack;
	}

	public static void setStaticBlack(BlackDependency staticBlack) {
		Bean.staticBlack = staticBlack;
	}

	private class InnerBean {
		public void method() {
			red.method1();
			blue.method1();
		}
	}

}
