package salve.contract.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import salve.BytecodeLoader;
import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassReader;
import salve.org.objectweb.asm.MethodVisitor;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.MethodVisitorAdapter;

public class OMIAnalyzer {
	private static final int QUICK_MODE = ClassReader.SKIP_CODE
			+ ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES;

	private static final String OBJECT = "java/lang/Object";

	private final ArrayList<String> omiMethods = new ArrayList<String>();

	public OMIAnalyzer analyze(byte[] bytecode, BytecodeLoader loader) {
		omiMethods.clear();

		Set<String> methods = new HashSet<String>();
		Set<String> visitedInterfaces = new HashSet<String>();

		ClassReader reader = new ClassReader(bytecode);
		if (OBJECT.equals(reader.getClassName())) {
			return this;
		}

		reader.accept(new MethodCollector(methods), QUICK_MODE);

		while (!OBJECT.equals(reader.getSuperName())) {
			for (String iface : reader.getInterfaces()) {
				visitInterface(iface, methods, visitedInterfaces, loader);
			}
			reader = new ClassReader(loader.loadBytecode(reader.getSuperName()));
			reader.accept(new OMIMethodIdentifier(methods), QUICK_MODE);
		}
		return this;
	}

	public boolean shouldInstrument(String name, String desc) {
		return omiMethods.contains(name + desc);
	}

	private void visitInterface(String iface, Set<String> methods,
			Set<String> visitedInterfaces, BytecodeLoader loader) {
		if (!visitedInterfaces.contains(iface)) {
			visitedInterfaces.add(iface);
			ClassReader reader = new ClassReader(loader.loadBytecode(iface));
			reader.accept(new OMIMethodIdentifier(methods), QUICK_MODE);
			for (String ifc : reader.getInterfaces()) {
				visitInterface(ifc, methods, visitedInterfaces, loader);
			}
		}
	}

	public static class MethodCollector extends ClassVisitorAdapter {
		private final Collection<String> methods;

		public MethodCollector(Collection<String> methods) {
			this.methods = methods;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			if (!"<init>".equals(name) && !"<clinit>".equals(name)) {
				methods.add(name + desc);
			}
			return null;
		}
	}

	public class OMIMethodIdentifier extends ClassVisitorAdapter implements
			Constants {
		private final Set<String> allowedMethods;

		public OMIMethodIdentifier(Set<String> allowedMethods) {
			this.allowedMethods = allowedMethods;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			final String method = name + desc;
			if (allowedMethods.contains(method)) {
				return new MethodVisitorAdapter() {
					@Override
					public AnnotationVisitor visitAnnotation(String desc,
							boolean visible) {
						if (OMI.getDescriptor().equals(desc)) {
							allowedMethods.remove(method);
							omiMethods.add(method);
						}
						return null;
					}
				};
			} else {
				return null;
			}
		}
	}
}
