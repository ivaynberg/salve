package salve.config;

import salve.Instrumentor;

public class Instrumentor1 implements Instrumentor {

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {
		return null;
	}

}
