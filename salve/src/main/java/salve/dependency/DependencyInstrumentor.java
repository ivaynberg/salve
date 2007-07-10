package salve.dependency;

import salve.Instrumentor;
import salve.dependency.impl.PojoInstrumentor;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

public class DependencyInstrumentor implements Instrumentor {

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {

		ClassPool pool = new ClassPool(ClassPool.getDefault());
		pool.appendClassPath(new LoaderClassPath(loader));
		pool.appendClassPath(new ByteArrayClassPath(name, bytecode));
		CtClass clazz = pool.get(name);
		PojoInstrumentor inst = new PojoInstrumentor(clazz);
		inst.instrument();
		return inst.getInstrumented().toBytecode();
	}
}
