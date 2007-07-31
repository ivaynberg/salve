package salve.spring.txn;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.org.objectweb.asm.ClassReader;
import salve.org.objectweb.asm.ClassWriter;

public class TransactionalInstrumentor implements Instrumentor {

	public byte[] instrument(String className, BytecodeLoader loader)
			throws InstrumentationException {

		try {
			final byte[] bytecode = loader.loadBytecode(className);
			ClassReader reader = new ClassReader(bytecode);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassInstrumentor inst = new ClassInstrumentor(writer);
			reader.accept(inst, 0);
			return writer.toByteArray();
		} catch (Exception e) {
			// TODO message
			throw new InstrumentationException(e);
		}
	}

}
