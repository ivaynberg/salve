package salve.asm;

import salve.dependency.Dependency;
import salve.dependency.DependencyLibrary;
import salve.dependency.Key;
import salve.dependency.KeyImpl;

public class TestBeanInstrumented {
	public static final Key _salvedepkey$red = new KeyImpl(RedDependency.class,
			TestBeanInstrumented.class, "_salvedepkey$red");

	@Dependency
	private RedDependency red;

	@Dependency
	private BlueDependency blue;

	public void execute() {
		_salveloc$blue();
		blue.method1();
		blue.method2();
	}

	private void _salveloc$blue() {
		if (blue == null) {
			Key key = new KeyImpl(BlueDependency.class,
					TestBeanInstrumented.class, "blue");
			blue = (BlueDependency) DependencyLibrary.locate(key);
		}
	}

}
