package salve.eclipse;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class Logger {
	private final ILog log;

	public Logger(ILog log) {
		this.log = log;
	}

	protected void info(String message) {
		Activator.getDefault().getLog().log(
				new Status(Status.INFO, Activator.PLUGIN_ID, message));
	}

	protected void log(String message, Throwable e) {
		Activator.getDefault().getLog().log(
				new Status(Status.INFO, Activator.PLUGIN_ID, message, e));
	}

}
