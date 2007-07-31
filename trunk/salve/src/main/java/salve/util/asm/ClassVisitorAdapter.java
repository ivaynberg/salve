package salve.util.asm;

import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.Attribute;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.FieldVisitor;
import salve.org.objectweb.asm.MethodVisitor;

public class ClassVisitorAdapter implements ClassVisitor {

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {

	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
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
		return null;
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	public void visitSource(String source, String debug) {
	}

}
