package salve.config;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.InstrumentorMonitor;

public class Instrumentor2 implements Instrumentor {

	public byte[] instrument(String className, BytecodeLoader loader, InstrumentorMonitor monitor)
			throws InstrumentationException {
		return null;
	}

}
