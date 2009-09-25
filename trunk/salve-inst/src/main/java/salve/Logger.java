package salve;

/**
 * Instrumentation logger used to communicate with the outside environment such
 * as an IDE or MAVEN
 * 
 * @author igor.vaynberg
 * 
 */
public interface Logger {
	/**
	 * Report an error
	 * 
	 * @param marker
	 * @param message
	 * @param params
	 */
	void error(CodeMarker marker, String message, Object... params);

	/**
	 * Report info message, useful for setting code markers in IDEs
	 * 
	 * @param marker
	 * @param message
	 * @param params
	 */
	void info(CodeMarker marker, String message, Object... params);

	/**
	 * Report a warning
	 * 
	 * @param marker
	 * @param message
	 * @param params
	 */
	void warn(CodeMarker marker, String message, Object... params);

}
