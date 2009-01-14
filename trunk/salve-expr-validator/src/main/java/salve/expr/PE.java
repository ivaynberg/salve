package salve.expr;

public class PE {
	private final String expression;

	public PE(Class<?> root, String expression) {
		this.expression = expression;
	}

	public PE(Class<?> root, String expression, String mode) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return expression;
	}

}
