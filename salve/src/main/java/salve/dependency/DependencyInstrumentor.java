package salve.dependency;

import java.io.FileOutputStream;

import salve.asm.loader.BytecodePool;
import salve.asm.loader.ClassLoaderLoader;
import salve.asm.loader.MemoryLoader;
import salve.dependency.impl.DependencyAnalyzer;
import salve.dependency.impl.DependencyInstrumentorAdapter;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassReader;
import salve.org.objectweb.asm.ClassWriter;

public class DependencyInstrumentor implements salve.Instrumentor {

	public static void main(String[] args) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		BytecodePool classPath = new BytecodePool();
		classPath.addLoader(new ClassLoaderLoader(loader));
		byte[] bytecode = classPath.loadBytecode("salve/dependency/Bean");
		ClassReader reader = new ClassReader(bytecode);
		DependencyAnalyzer locator = new DependencyAnalyzer(classPath);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassAdapter adapter = new DependencyInstrumentorAdapter(writer,
				locator);
		reader.accept(adapter, 0);

		classPath.save("salve/dependency/Bean", writer.toByteArray());

		FileOutputStream out = new FileOutputStream(
				"target/test-classes/Test.class");
		final byte[] bytes = writer.toByteArray();
		out.write(writer.toByteArray());
		out.close();

		Class tb = classPath.loadClass("salve/dependency/Bean");

		Object b = tb.newInstance();
		System.out.println(b.getClass().getName());
		Bean bb = (Bean) b;
		bb.method1();

		/*
		 * reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
		 * 
		 * ClassReader instr = new ClassReader(writer.toByteArray());
		 * StringBuilderOutputStream sbos = new StringBuilderOutputStream();
		 * instr.accept(new ASMifierClassVisitor(new PrintWriter(sbos)), 0);
		 * 
		 * ClassReader targetr = new ClassReader(loader
		 * .getResourceAsStream("salve/asm/TestBeanInstrumented.class"));
		 * StringBuilderOutputStream tsbos = new StringBuilderOutputStream();
		 * targetr.accept(new ASMifierClassVisitor(new PrintWriter(tsbos)), 0);
		 * 
		 * String s = sbos.getBuilder().toString(); String t =
		 * tsbos.getBuilder().toString(); s = s.replace("\r\n", "\n"); s =
		 * s.replace("\n\r", "\n"); t = t.replace("\r\n", "\n"); t =
		 * t.replace("\n\r", "\n");
		 * 
		 * t = t.replace("TestBeanInstrumented", "TestBean");
		 * 
		 * MyersDiff diff = new MyersDiff(); Revision r =
		 * diff.diff(s.split("\n"), t.split("\n")); //
		 * System.out.println(r.toString());
		 * 
		 * System.out.println(tsbos.getBuilder().toString());
		 */
	}

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {

		BytecodePool pool = new BytecodePool();
		pool.addLoader(new MemoryLoader(name, bytecode));
		pool.addLoader(new ClassLoaderLoader(loader));
		if (Object.class.getClassLoader() != null) {
			pool
					.addLoader(new ClassLoaderLoader(Object.class
							.getClassLoader()));
		} else {
			pool.addLoader(new ClassLoaderLoader(Thread.currentThread()
					.getContextClassLoader()));
		}

		DependencyAnalyzer analyzer = new DependencyAnalyzer(pool);
		ClassReader reader = new ClassReader(bytecode);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		DependencyInstrumentorAdapter inst = new DependencyInstrumentorAdapter(
				writer, analyzer);
		reader.accept(inst, 0);

		return writer.toByteArray();
	}
}
