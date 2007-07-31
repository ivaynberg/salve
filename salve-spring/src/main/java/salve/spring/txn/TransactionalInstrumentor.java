package salve.spring.txn;

import salve.Instrumentor;
import salve.org.objectweb.asm.ClassReader;
import salve.org.objectweb.asm.ClassWriter;

public class TransactionalInstrumentor implements Instrumentor {

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {

		ClassReader reader = new ClassReader(bytecode);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassInstrumentor inst = new ClassInstrumentor(writer);
		reader.accept(inst, 0);
		return writer.toByteArray();
	}

}
