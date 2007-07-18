package salve.asm;

import salve.dependency.DependencyLibrary;
import salve.dependency.Key;
import salve.dependency.KeyImpl;

public class TestBeanInstrumented {
	public static final Key _salvedepkey$dependency = new KeyImpl(
			RedDependency.class, TestBeanInstrumented.class,
			"_salvedepkey$dependency");

	public int doit() {
		int a = 4;
		int b = 5;
		b = a;
		return a;
	}

	public RedDependency getDependency() {
		RedDependency _localdependency = (RedDependency) DependencyLibrary
				.locate(_salvedepkey$dependency);

		return _localdependency;
	}

}
