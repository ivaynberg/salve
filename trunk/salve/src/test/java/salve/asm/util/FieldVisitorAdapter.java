package salve.asm.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

public class FieldVisitorAdapter implements FieldVisitor {

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

		return null;
	}

	public void visitAttribute(Attribute attr) {

	}

	public void visitEnd() {

	}

}
