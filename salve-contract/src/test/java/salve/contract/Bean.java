package salve.contract;

public class Bean {
	public static final Object NULL = new Object();

	@NotNull
	public Object testNotNull(@NotNull
	Object arg) {
		return arg == NULL ? null : arg;
	}

}
