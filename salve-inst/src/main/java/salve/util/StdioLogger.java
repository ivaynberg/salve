package salve.util;

import salve.CodeMarker;
import salve.Logger;

public class StdioLogger implements Logger {

	public static final Logger INSTANCE = new StdioLogger();

	private static String message(String prefix, CodeMarker marker, String message, Object... params) {
		StringBuilder buff = new StringBuilder();
		if (marker != null) {
			buff.append(prefix + " " + marker.getClassName() + ":" + marker.getLineNumber() + ": ");
			buff.append(String.format(message, params));
		}
		return buff.toString();
	}

	public void error(CodeMarker marker, String message, Object... params) {
		System.err.println(message("[SALVE ERROR]", marker, message, params));
	}

	public void info(CodeMarker marker, String message, Object... params) {
		System.out.println(message(" [SALVE INFO]", marker, message, params));

	}

	public void warn(CodeMarker marker, String message, Object... params) {
		System.err.println(message(" [SALVE WARN]", marker, message, params));
	}

}