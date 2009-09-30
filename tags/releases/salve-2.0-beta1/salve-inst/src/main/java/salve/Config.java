package salve;

import java.util.Collection;

public interface Config {
	/**
	 * Retrieves a collection of instrumentors that should be applied to the
	 * specified class.
	 * 
	 * @param className
	 *            binary class name
	 * @return collection of instrumentors
	 */
	Collection<Instrumentor> getInstrumentors(String className);

	/**
	 * Retrieves {@link Scope} for the specified instrumentor. This scope can be
	 * used by the instrumentor to more efficiently filter which classes should
	 * be instrumented.
	 * 
	 * @param instrumentor
	 * @return instrumentor scope
	 * 
	 *         FIXME see if this can be removed, not very useful
	 */
	Scope getScope(Instrumentor instrumentor);
}
