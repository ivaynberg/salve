package salve.monitor;

import salve.InstrumentorMonitor;

public class ModificationMonitor implements InstrumentorMonitor {
	private boolean modified = false;

	public void fieldAdded(String className, int fieldAccess, String fieldName, String fieldDesc) {
		modified = true;
	}

	public void fieldModified(String className, int fieldAccess, String fieldName, String fieldDesc) {
		modified = true;
	}

	public void fieldRemoved(String className, int fieldAccess, String fieldName, String fieldDesc) {
		modified = true;
	}

	public boolean isModified() {
		return modified;
	}

	public void methodAdded(String className, int methodAccess, String methodName, String methodDesc) {
		modified = true;
	}

	public void methodModified(String className, int methodAccess, String methodName, String methodDesc) {
		modified = true;
	}

	public void methodRemoved(String className, int methodAccess, String methodName, String methodDesc) {
		modified = true;
	}

}
