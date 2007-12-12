package salve.depend.spring.txn;

import java.util.HashSet;
import java.util.Set;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.ClassVisitor;
import salve.asmlib.FieldVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.util.asm.MethodVisitorAdapter;

public class ClassAnalyzerImpl implements ClassVisitor, Constants,
		ClassAnalyzer, Opcodes {

	private Set<String> methods = new HashSet<String>();
	private boolean annotated = false;

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

		if (TRANSACTIONAL_DESC.equals(desc)) {
			annotated = true;
		}
		return null;
	}

	public void visitAttribute(Attribute attr) {
	}

	public void visitEnd() {
	}

	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		return null;
	}

	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		if ((access & ACC_STATIC) != 0) {
			// static methods never need to be instrumented by @Transactional
			return null;
		} else if ("<init>".equals(name)) {
			// constructors cannot be marked with @Transactional
			return null;
		}

		final String methodId = toString(access, name, desc, signature,
				exceptions);
		if (annotated) {
			methods.add(methodId);
			return null;
		} else {
			return new MethodVisitorAdapter() {
				@Override
				public AnnotationVisitor visitAnnotation(String desc,
						boolean visible) {
					if (TRANSACTIONAL_DESC.equals(desc)) {
						methods.add(methodId);
					}
					return null;
				}
			};
		}
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	public void visitSource(String source, String debug) {
	}

	private String toString(int access, String name, String desc,
			String signature, String[] exceptions) {
		StringBuilder str = new StringBuilder();
		str.append(access).append(name).append(desc).append(signature);
		if (exceptions != null) {
			for (String exception : exceptions) {
				str.append(exception);
			}
		}
		return str.toString();
	}

	public boolean shouldInstrument(int access, String name, String desc,
			String sig, String[] exceptions) {
		return methods.contains(toString(access, name, desc, sig, exceptions));
	}

}
