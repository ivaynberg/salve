package salve.dependency.impl;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.FieldVisitor;
import salve.asmlib.Type;
import salve.dependency.Dependency;

public class DependencyAnnotRemover implements FieldVisitor {
	private static final String DESC = Type.getDescriptor(Dependency.class);
	private final FieldVisitor delegate;

	public DependencyAnnotRemover(FieldVisitor delegate) {
		super();
		this.delegate = delegate;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (DESC.equals(desc)) {
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
