package salve;

/**
 * Represents a bytecode instrumentor
 * 
 * @author ivaynberg
 */
public interface Instrumentor {
	/**
	 * Instruments bytecode
	 * 
	 * @param loader
	 *            defining classloader of the class being transformed, maybe
	 *            null if bootstrapping classloader
	 * @param name
	 *            fully qualified class name
	 * @param bytecode
	 *            bytecode
	 * @return instrumented bytecode
	 * @throws Exception
	 *             if instrumentation fails
	 */
	byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception;
}
