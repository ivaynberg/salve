package salve.dependency;

public class Bean {

	// force this bean to have a clinit
	@SuppressWarnings("unused")
	private static long FORCE_CLINIT = System.currentTimeMillis();

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
		// System.out.println("1: " + red);
		red.method1();
		// System.out.println("2: " + red);
		Object tmp = null;
		if (tmp == null) {
			// System.out.println("3: " + red);
			// System.out.println(red);
			tmp = red.method2();
		}
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

}
