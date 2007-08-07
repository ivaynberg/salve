package salve.monitor;

import salve.InstrumentorMonitor;

public class NoopMonitor implements InstrumentorMonitor {
	public static final InstrumentorMonitor INSTANCE = new NoopMonitor();

	private NoopMonitor() {

	}

	public void fieldAdded(String className, int fieldAccess, String fieldName, String fieldDesc) {

	}

	public void fieldModified(String className, int fieldAccess, String fieldName, String fieldDesc) {

	}

	public void fieldRemoved(String className, int fieldAccess, String fieldName, String fieldDesc) {

	}

	public void methodAdded(String className, int methodAccess, String methodName, String methodDesc) {

	}

	public void methodModified(String className, int methodAccess, String methodName, String methodDesc) {

	}

	public void methodRemoved(String className, int methodAccess, String methodName, String methodDesc) {

	}

}
