package salve.dependency.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import salve.asm.BytecodeLoader;
import salve.asm.CannotLoadBytecodeException;
import salve.asm.util.AnnotationVisitorAdapter;
import salve.asm.util.ClassVisitorAdapter;
import salve.asm.util.FieldVisitorAdapter;
import salve.dependency.Dependency;
import salve.dependency.InjectionStrategy;
import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassReader;
import salve.org.objectweb.asm.FieldVisitor;
import salve.org.objectweb.asm.Type;

public class DependencyAnalyzer {
	private static final Type DEPENDENCY_TYPE = Type.getType(Dependency.class);
	private final BytecodeLoader loader;
	private final Set<String> owners = new HashSet<String>();
	private final Map<String, DependencyField> fields = new HashMap<String, DependencyField>();

	public DependencyAnalyzer(BytecodeLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException(
					"Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	public DependencyField locateField(String owner, String name) {
		// TODO check args
		processClass(owner);
		return fields.get(fieldKey(owner, name));
	}

	public Collection<DependencyField> locateFields(String owner) {
		// TODO check args
		processClass(owner);
		List<DependencyField> matches = new ArrayList<DependencyField>();
		for (DependencyField field : fields.values()) {
			if (field.getOwner().equals(owner)) {
				matches.add(field);
			}
		}
		return matches;

	}

	/**
	 * @param owner
	 */
	private void processClass(String owner) {
		if (!owners.contains(owner)) {
			byte[] bytecode = loader.loadBytecode(owner);
			if (bytecode == null) {
				throw new CannotLoadBytecodeException(owner);
			}
			ClassReader reader = new ClassReader(bytecode);
			reader.accept(new Analyzer(), ClassReader.SKIP_CODE
					+ ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);

			owners.add(owner);
		}
	}

	private static String fieldKey(String owner, String name) {
		return owner + "." + name;
	}

	private class Analyzer extends ClassVisitorAdapter {
		private String owner;

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			owner = name;
		}

		@Override
		public FieldVisitor visitField(final int fieldAccess,
				final String fieldName, final String fieldDesc,
				String signature, Object value) {
			return new FieldVisitorAdapter() {

				@Override
				public AnnotationVisitor visitAnnotation(String desc,
						boolean visible) {
					if (visible) {
						return visitFieldAnnotation(fieldAccess, fieldName,
								fieldDesc, desc);
					}
					return null;
				}
			};
		}

		private AnnotationVisitor visitFieldAnnotation(int fieldAcess,
				String fieldName, String fieldDesc, String annotDesc) {
			if (Type.getType(annotDesc).equals(DEPENDENCY_TYPE)) {
				final DependencyField field = new DependencyField(owner,
						fieldName, fieldDesc);

				fields.put(fieldKey(owner, fieldName), field);
				return new AnnotationVisitorAdapter() {

					@Override
					public void visitEnum(String name, String desc, String value) {

						if ("strategy".equals(name)) {
							field.setStrategy(InjectionStrategy.valueOf(value));
						}
					}

				};
			}
			return null;

		}
	}
}
