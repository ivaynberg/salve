package salve.util.asm;

import salve.asmlib.AnnotationVisitor;

public class AnnotationAdapter implements AnnotationVisitor {
	protected final AnnotationVisitor av;

	public AnnotationAdapter(AnnotationVisitor av) {
		this.av = av;
	}

	public void visit(String name, Object value) {
		av.visit(name, value);
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return av.visitAnnotation(name, desc);
	}

	public AnnotationVisitor visitArray(String name) {
		return av.visitArray(name);
	}

	public void visitEnd() {
		av.visitEnd();
	}

	public void visitEnum(String name, String desc, String value) {
		av.visitEnum(name, desc, value);
	}

}
