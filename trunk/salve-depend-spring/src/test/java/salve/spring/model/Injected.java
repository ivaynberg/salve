package salve.spring.model;

import salve.depend.Dependency;
import salve.spring.SpringBeanId;

public class Injected {
	// test lookup by spring id
	@Dependency
	@SpringBeanId("somea")
	private A a;
	// test lookup by defaulting to field name
	@Dependency
	private A b;

	// test lookup by type alone
	@Dependency
	private C c;

	// test more then one of type error
	@Dependency
	private D d;

	// test not found
	@Dependency
	private E e;

	public A getA() {
		return a;
	}

	public A getB() {
		return b;
	}

	public C getC() {
		return c;
	}

	public D getD() {
		return d;
	}

	public E getE() {
		return e;
	}

}
