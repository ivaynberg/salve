package salve.depend.impl;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.FieldVisitor;

public class DependencyAnnotRemover implements FieldVisitor {
	private final FieldVisitor delegate;

	public DependencyAnnotRemover(FieldVisitor delegate) {
		super();
		this.delegate = delegate;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (Constants.DEP_DESC.equals(desc)) {
			return null;
		} else {
			return delegate.visitAnnotation(desc, visible);
		}
	}

	public void visitAttribute(Attribute attr) {
		delegate.visitAttribute(attr);
	}

	public void visitEnd() {
		delegate.visitEnd();
	}

}
