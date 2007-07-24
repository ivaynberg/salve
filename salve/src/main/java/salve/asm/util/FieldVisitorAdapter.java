package salve.asm.util;

import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.Attribute;
import salve.org.objectweb.asm.FieldVisitor;

public class FieldVisitorAdapter implements FieldVisitor {

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

		return null;
	}

	public void visitAttribute(Attribute attr) {

	}

	public void visitEnd() {

	}

}
