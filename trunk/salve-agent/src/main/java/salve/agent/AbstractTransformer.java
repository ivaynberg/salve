package salve.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import salve.Config;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.MemoryLoader;
import salve.monitor.NoopMonitor;

/**
 * Base class for class transformers. This class takes care of everything except
 * loading salve configuration ({@link Config})
 * 
 * @author ivaynberg
 * 
 */
public abstract class AbstractTransformer implements ClassFileTransformer {
	/**
	 * Gets configuration for the given class name and class loader
	 * 
	 * @param loader
	 * @param className
	 * @return {@link Config} for class
	 */
	protected abstract Config getConfig(ClassLoader loader, String className);

	/**
	 * Instruments class
	 * 
	 * @param loader
	 *            class loader
	 * @param className
	 *            binary class name (eg salve/agent/Agent)
	 * @param bytecode
	 *            bytecode
	 * @return instrumented bytecode
	 */
	private byte[] instrument(ClassLoader loader, String className,
			byte[] bytecode) {
		final Config config = getConfig(loader, className);
		try {
			for (Instrumentor inst : config.getInstrumentors(className)) {
				CompoundLoader bl = new CompoundLoader();
				bl.addLoader(new MemoryLoader(className, bytecode));
				bl.addLoader(new ClassLoaderLoader(loader));

				InstrumentationContext ctx = new InstrumentationContext(bl,
						NoopMonitor.INSTANCE, config.getScope(inst));

				bytecode = inst.instrument(className, ctx);
			}
			return bytecode;
		} catch (InstrumentationException e) {
			throw new RuntimeException("Error instrumenting class: "
					+ className, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		// instrument bytecode
		return instrument(loader, className, classfileBuffer);
	}
}
