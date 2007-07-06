package salve.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import salve.bytecode.PojoInstrumentor;

public class Agent {
	private static Instrumentation INSTRUMENTATION;

	private static final String[] EXCLUSIONS = { "java/", "sun/", "com/sun",
			"javax/" };

	public static void premain(String agentArgs, Instrumentation inst) {
		// ignore double agents
		if (INSTRUMENTATION == null) {
			INSTRUMENTATION = inst;
			inst.addTransformer(new Transformer());
		}
	}

	private static class Transformer implements ClassFileTransformer {

		public byte[] transform(ClassLoader loader, String className,
				Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer)
				throws IllegalClassFormatException {

			// skip some common classes
			for (String exclusion : EXCLUSIONS) {
				if (className.startsWith(exclusion)) {
					return classfileBuffer;
				}
			}

			try {
				// System.out.println(">>> Instrumenting class [" + className
				// + "]");
				final String name = className.replace("/", ".");
				ClassPool pool = ClassPool.getDefault();
				pool.insertClassPath(new ByteArrayClassPath(name,
						classfileBuffer));

				CtClass clazz = pool.get(name);
				PojoInstrumentor inst = new PojoInstrumentor(clazz);
				if (inst.instrument()) {
					byte[] bytecode = inst.getInstrumented().toBytecode();
					clazz.detach();
					return bytecode;
				} else {
					return classfileBuffer;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		}
	}

}
