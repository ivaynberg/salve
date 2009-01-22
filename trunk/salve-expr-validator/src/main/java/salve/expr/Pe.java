package salve.expr;

public class Pe {
	private final String expression;

	public Pe(Class<?> root, String expression) {
		this.expression = expression;
	}

	public Pe(Class<?> root, String expression, String mode) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return expression;
	}

}
