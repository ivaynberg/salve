package salve.expr;

public class PE2 {
	private final String expression;

	public PE2(Class<?> root, String expression) {
		this.expression = expression;
	}

	public PE2(Class<?> root, String expression, String mode) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return expression;
	}

}
