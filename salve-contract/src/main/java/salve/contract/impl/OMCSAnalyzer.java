package salve.contract.impl;

import salve.BytecodeLoader;

public class OMCSAnalyzer {
	private final String owner;
	private final BytecodeLoader loader;

	public OMCSAnalyzer(String className, BytecodeLoader loader) {
		owner = className;
		this.loader = loader;
	}

	public OMCSAnalyzer analyze() {

		return this;
	}
}
