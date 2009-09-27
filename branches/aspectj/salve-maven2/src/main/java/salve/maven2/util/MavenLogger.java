package salve.maven2.util;

import org.apache.maven.plugin.logging.Log;

import salve.CodeMarker;
import salve.Logger;

public class MavenLogger implements Logger {
	private static String message(CodeMarker marker, String message, Object... params) {
		StringBuilder buff = new StringBuilder();
		if (marker != null) {
			buff.append(marker.getClassName() + ":" + marker.getLineNumber() + ": ");
			buff.append(String.format(message, params));
		}
		return buff.toString();
	}

	private final Log log;
	private boolean hasErrors = false;

	public MavenLogger(Log log) {
		this.log = log;
	}

	public void error(CodeMarker marker, String message, Object... params) {
		log.error(message(marker, message, params));
		hasErrors = true;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public void info(CodeMarker marker, String message, Object... params) {
		log.info(message(marker, message, params));

	}

	public void warn(CodeMarker marker, String message, Object... params) {
		log.warn(message(marker, message, params));
	}

}
