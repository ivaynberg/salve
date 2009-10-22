package salve.util;

import salve.CodeMarker;
import salve.Logger;

public class NoopLogger implements Logger {

	public static Logger INSTANCE = new NoopLogger();

	public void error(CodeMarker marker, String message, Object... params) {
	}

	public void info(CodeMarker marker, String message, Object... params) {

	}

	public void warn(CodeMarker marker, String message, Object... params) {
	}

}
