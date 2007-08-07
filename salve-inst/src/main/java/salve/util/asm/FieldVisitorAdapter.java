package salve.util.asm;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.FieldVisitor;

public class FieldVisitorAdapter implements FieldVisitor {

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

		return null;
	}

	public void visitAttribute(Attribute attr) {

	}

	public void visitEnd() {

	}

}
