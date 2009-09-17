package salve.model;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.FieldVisitor;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.util.asm.AnnotationAdapter;
import salve.util.asm.FieldVisitorAdapter;

public class ModelUpdateVisitor extends ClassAdapter {

	private static class ModelAnnotationVisitor extends AnnotationAdapter {
		private final AnnotationModel annot;

		public ModelAnnotationVisitor(AnnotationVisitor av, AnnotationModel annot) {
			super(av);
			this.annot = annot;
		}

		@Override
		public void visit(String name, Object value) {
			annot.add(new AnnotationModel.ValueField(name, value));
			super.visit(name, value);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			// TODO Auto-generated method stub
			return super.visitAnnotation(name, desc);
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			// TODO Auto-generated method stub
			return super.visitArray(name);
		}

		@Override
		public void visitEnum(String name, String desc, String value) {
			annot.add(new AnnotationModel.EnumField(name, desc, value));
			super.visitEnum(name, desc, value);
		}
	}

	private static class ModelFieldVisitor extends FieldVisitorAdapter {
		private final FieldModel field;

		public ModelFieldVisitor(FieldModel field) {
			this.field = field;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			AnnotationModel am = new AnnotationModel(desc, visible);
			return super.visitAnnotation(desc, visible);
		}
	}

	private static class ModelMethodVisitor extends MethodAdapter {

		private final MethodModel method;

		public ModelMethodVisitor(MethodVisitor mv, MethodModel method) {
			super(mv);
			this.method = method;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			AnnotationModel annot = new AnnotationModel(desc, visible);
			method.add(annot);
			return new ModelAnnotationVisitor(super.visitAnnotation(desc, visible), annot);
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			if (index > 0 && index < method.getArgCount()) {
				method.setArgName(index, name);
			}
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			AnnotationModel annot = new AnnotationModel(desc, visible);
			method.addParameterAnnot(parameter, annot);
			return new ModelAnnotationVisitor(super.visitParameterAnnotation(parameter, desc, visible), annot);
		}

	}

	private final ProjectModel model;

	private ClassModel current;

	public ModelUpdateVisitor(ProjectModel model, ClassVisitor cv) {
		super(cv);
		this.model = model;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		current = new ClassModel(model, access, name, signature, superName, interfaces);
		model.add(current);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		AnnotationModel am = new AnnotationModel(desc, visible);
		current.add(am);
		return new ModelAnnotationVisitor(super.visitAnnotation(desc, visible), am);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		FieldModel fm = new FieldModel(access, name, desc, signature);
		current.add(fm);
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		InnerClassModel icm = new InnerClassModel(name, innerName, access);
		current.add(icm);
		super.visitInnerClass(name, outerName, innerName, access);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodModel mm = new MethodModel(current, access, name, desc, signature, exceptions);
		current.add(mm);
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new ModelMethodVisitor(mv, mm);
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		current.setOuter(new OuterClassModel(owner, name, desc));
		super.visitOuterClass(owner, name, desc);
	}

}
