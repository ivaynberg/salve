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
	 * @param className
	 *            fully qualified class name (salve/asm/BytecodeLoader)
	 * @param loader
	 *            bytecodeLoader that can be used to load bytecode for specified
	 *            class and any referenced classes
	 * @return instrumented bytecode
	 * @throws Exception
	 *             if instrumentation fails
	 */
	byte[] instrument(String className, BytecodeLoader loader) throws InstrumentationException;
}
