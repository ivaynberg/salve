package salve.spring.txn;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.InstrumentorMonitor;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;

public class TransactionalInstrumentor implements Instrumentor {

	public byte[] instrument(String className, BytecodeLoader loader,
			InstrumentorMonitor monitor) throws InstrumentationException {

		try {
			final byte[] bytecode = loader.loadBytecode(className);
			ClassReader reader = new ClassReader(bytecode);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassInstrumentor inst = new ClassInstrumentor(writer, monitor);
			reader.accept(inst, 0);
			return writer.toByteArray();
		} catch (Exception e) {
			// TODO message
			throw new InstrumentationException(e);
		}
	}

}
