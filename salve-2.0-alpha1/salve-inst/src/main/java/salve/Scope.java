package salve;

public interface Scope {
	/**
	 * Scope that includes all classes
	 */
	public static final Scope ALL = new Scope() {

		/**
		 * {@inheritDoc}
		 */
		public boolean includes(String className) {
			return true;
		}

	};

	/**
	 * Scope that includes no classes
	 */
	public static final Scope NONE = new Scope() {

		/**
		 * {@inheritDoc}
		 */
		public boolean includes(String className) {
			return false;
		}

	};

	/**
	 * Checks if the specified class is within the instrumentation context.
	 * Classes that are not within the context should be ignored by
	 * instrumentors.
	 * 
	 * @param className
	 *            binary class name (eg salve/asm/BytecodeLoader)
	 * @return true if the class is within instrumentation context
	 */
	boolean includes(String className);

}
