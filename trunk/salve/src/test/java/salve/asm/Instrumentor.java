package salve.asm;

import java.io.FileOutputStream;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import salve.asm.loader.ClassLoaderLoader;
import salve.asm.loader.BytecodePool;

public class Instrumentor {

	public static void main(String[] args) throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		BytecodePool classPath = new BytecodePool();
		classPath.addLoader(new ClassLoaderLoader(loader));
		byte[] bytecode = classPath.load("salve/asm/TestBean");
		ClassReader reader = new ClassReader(bytecode);
		DependencyAnalyzer analyzer = new DependencyAnalyzer();
		reader.accept(analyzer, ClassReader.SKIP_DEBUG + ClassReader.SKIP_CODE);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassAdapter adapter = new DependencyInstrumentor(writer, analyzer);
		reader.accept(adapter, 0);

		classPath.save("salve/asm/TestBean", writer.toByteArray());

		FileOutputStream out = new FileOutputStream(
				"target/test-classes/Test.class");
		final byte[] bytes = writer.toByteArray();
		out.write(writer.toByteArray());
		out.close();

		Class tb = classPath.loadClass("salve/asm/TestBean");

		Object b = tb.newInstance();
		System.out.println(b.getClass().getName());
		TestBean bb = (TestBean) b;
		bb.getDependency();

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

}
