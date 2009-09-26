package salve;

import salve.model.CtProject;

/**
 * Represents context in which the instrumentor runs
 * 
 * @author igor.vaynberg
 * 
 */
public class InstrumentationContext {

	private final BytecodeLoader loader;
	private final Scope scope;
	private final CtProject model;
	private final Logger logger;

	/**
	 * Constructor
	 * 
	 * @param loader
	 *            bytecode loader the instrumentor can use to access bytecode
	 * @param scope
	 *            scope used to identify classes that are within instrumentation
	 *            scope
	 */
	public InstrumentationContext(BytecodeLoader loader, Scope scope, CtProject model, Logger logger) {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		if (scope == null) {
			throw new IllegalArgumentException("Argument `scope` cannot be null");
		}
		if (model == null) {
			throw new IllegalArgumentException("Argument `model` cannot be null");
		}
		if (logger == null) {
			throw new IllegalArgumentException("Argument `logger` cannot be null");
		}

		this.loader = loader;
		this.scope = scope;
		this.model = model;
		this.logger = logger;
	}

	/**
	 * @return bytecode loader the instrumentor can use to access bytecode
	 */
	public BytecodeLoader getLoader() {
		return loader;
	}

	/**
	 * 
	 * @return logger
	 */
	public Logger getLogger() {
		return logger;
	}

	public CtProject getModel() {
		return model;
	}

	/**
	 * @return scope used to identify classes that are within instrumentation
	 *         scope
	 */
	public Scope getScope() {
		return scope;
	}

}
