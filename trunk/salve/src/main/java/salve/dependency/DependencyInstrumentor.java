package salve.dependency;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import salve.Instrumentor;
import salve.dependency.impl.PojoInstrumentor;

public class DependencyInstrumentor implements Instrumentor {

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {

		ClassPool pool = new ClassPool();
		pool.appendClassPath(new ByteArrayClassPath(name, bytecode));
		pool.appendClassPath(new LoaderClassPath(loader));
		pool.appendSystemPath();
		CtClass clazz = pool.get(name);
		PojoInstrumentor inst = new PojoInstrumentor(clazz);
		inst.instrument();
		return inst.getInstrumented().toBytecode();
	}
}
