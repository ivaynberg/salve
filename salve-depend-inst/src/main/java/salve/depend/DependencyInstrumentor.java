package salve.depend;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.depend.impl.ClassAnalyzer;
import salve.depend.impl.ClassInstrumentor;

public class DependencyInstrumentor implements Instrumentor {

	public byte[] instrument(String className, BytecodeLoader loader) throws InstrumentationException {
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		if (className == null) {
			throw new IllegalArgumentException("Argument `className` cannot be null");
		}

		className = className.trim();

		if (className.length() == 0) {
			throw new IllegalArgumentException("Argument `className` cannot be an empty");
		}

		try {
			byte[] bytecode = loader.loadBytecode(className);
			if (bytecode == null) {
				throw new CannotLoadBytecodeException(className);
			}

			ClassAnalyzer analyzer = new ClassAnalyzer(loader);
			ClassReader reader = new ClassReader(bytecode);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassInstrumentor inst = new ClassInstrumentor(writer, analyzer);
			reader.accept(inst, 0);

			return writer.toByteArray();
		} catch (Exception e) {
			// TODO message
			throw new InstrumentationException(e);
		}
	}
}
