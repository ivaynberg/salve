package salve.util.asm;

import salve.org.objectweb.asm.AnnotationVisitor;

public class AnnotationVisitorAdapter implements AnnotationVisitor {

	public void visit(String name, Object value) {

	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return null;
	}

	public AnnotationVisitor visitArray(String name) {
		return null;
	}

	public void visitEnd() {
	}

	public void visitEnum(String name, String desc, String value) {
	}

}
