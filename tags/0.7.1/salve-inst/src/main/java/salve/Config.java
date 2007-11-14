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
}
