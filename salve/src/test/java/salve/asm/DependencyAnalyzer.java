/**
 * 
 */
package salve.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import salve.asm.util.AnnotationVisitorAdapter;
import salve.asm.util.ClassVisitorAdapter;
import salve.asm.util.FieldVisitorAdapter;
import salve.dependency.Dependency;
import salve.dependency.InjectionStrategy;

class DependencyAnalyzer extends ClassVisitorAdapter {
	private static final Type DEPENDENCY_TYPE = Type.getType(Dependency.class);
	private List<DependencyField> dependencies;
	private String className;

	public List<DependencyField> getDependencies() {
		return dependencies;
	}

	public DependencyField getDependencyForField(String name) {
		for (DependencyField field : dependencies) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		dependencies = new ArrayList<DependencyField>();
		className = name;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {

		final DependencyField field = new DependencyField(className, name, desc);
		return new FieldVisitorAdapter() {

			@Override
			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				if (visible) {
					if (Type.getType(desc).equals(DEPENDENCY_TYPE)) {
						dependencies.add(field);
						return new AnnotationVisitorAdapter() {

							@Override
							public void visitEnum(String name, String desc,
									String value) {

								if ("strategy".equals(name)) {
									field.setStrategy(InjectionStrategy
											.valueOf(value));
								}
							}

						};
					}
					return null;
				}
				return null;
			}
		};
	}
}