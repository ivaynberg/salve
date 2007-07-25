package salve.dependency.impl;

import salve.dependency.Dependency;
import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.Attribute;
import salve.org.objectweb.asm.FieldVisitor;
import salve.org.objectweb.asm.Type;

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
