package salve.util;

import salve.CodeMarker;
import salve.InstrumentationException;
import salve.Logger;

public class TestLogger implements Logger {

	public static Logger INSTANCE = new TestLogger();

	public void error(CodeMarker marker, String message, Object... params) {
		throw new InstrumentationException(String.format(message, params), marker);
	}

	public void info(CodeMarker marker, String message, Object... params) {

	}

	public void warn(CodeMarker marker, String message, Object... params) {
	}

}
