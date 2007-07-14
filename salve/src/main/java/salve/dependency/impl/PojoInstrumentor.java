package salve.dependency.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import salve.dependency.Dependency;
import salve.dependency.DependencyLibrary;
import salve.dependency.InjectionStrategy;
import salve.dependency.Key;
import salve.dependency.KeyImpl;

public class PojoInstrumentor {
	private static final String LOCATOR_METHOD_PREFIX = "__locate";

	private static final String LOCATOR_CALL = DependencyLibrary.class
			.getName()
			+ ".locate";

	private final CtClass pojo;

	private final List<CtField> annotatedFields = new ArrayList<CtField>();

	public PojoInstrumentor(final CtClass pojo) {
		super();
		this.pojo = pojo;
	}

	public CtClass getInstrumented() {
		return pojo;
	}

	public boolean instrument() throws ClassNotFoundException,
			CannotCompileException, NotFoundException {
		collectAnnotatedFields();

		if (annotatedFields.isEmpty()) {
			return false;
		}

		// instrument base functionality into class
		addDependencyKeys();
		addLazyInitLocators();

		// instrument field access
		for (CtBehavior behavior : pojo.getDeclaredBehaviors()) {
			if (!behavior.getName().startsWith(LOCATOR_METHOD_PREFIX)) {
				behavior.instrument(new FieldAccessEditor());
			}
		}

		// cleanup
		for (CtField field : annotatedFields) {
			Dependency dep = findDependencyAnnot(field);
			if (dep.strategy() == InjectionStrategy.REMOVE_FIELD) {
				pojo.removeField(field);
			}
		}

		return true;
	}

	private void addDependencyKeys() throws CannotCompileException,
			NotFoundException, ClassNotFoundException {
		// TODO inner non-static classes cannot have static fields :|
		for (CtField field : annotatedFields) {
			final String type = Key.class.getName();
			final String keyimpl = KeyImpl.class.getName();
			final String name = DependencyConstants.keyFieldName(field
					.getName());

			final String src = "public static final " + type + " " + name
					+ "=new " + keyimpl + "(" + field.getType().getName()
					+ ".class," + pojo.getName() + ".class, \""
					+ field.getName() + "\");";
			CtField keyHolder = CtField.make(src, pojo);

			AnnotationsAttribute attr = (AnnotationsAttribute) field
					.getFieldInfo().getAttribute(
							AnnotationsAttribute.visibleTag);

			Annotation[] annots = attr.getAnnotations();
			keyHolder.getFieldInfo().addAttribute(attr);

			pojo.addField(keyHolder);
		}
	}

	private void addLazyInitLocators() throws ClassNotFoundException,
			NotFoundException, CannotCompileException {
		for (CtField field : annotatedFields) {

			Dependency dep = findDependencyAnnot(field);

			if (dep.strategy() != InjectionStrategy.REMOVE_FIELD) {

				final String type = field.getType().getName();
				final String typeclass = type + ".class";
				final String name = locatorMethodName(field);
				final String lib = DependencyLibrary.class.getName();
				final String fn = field.getName();

				final StringBuilder code = new StringBuilder();
				code.append("private ").append(type).append(" ").append(name)
						.append("() {");

				code.append("if (").append(fn).append("==null) {");

				// Class proxy=new
				// salve.bytecode.ProxyBuilder(typeclass).build().toClass();
				code.append("java.lang.Class proxy=new ").append(
						ProxyBuilder.class.getName()).append("(")

				.append(typeclass).append(").build().toClass();");

				// fn=(type) proxy.getConstructor(new Class[] { salve.Key.class
				// }).newInstance(new Object[] { key });

				code.append(fn).append("=(").append(type).append(
						") proxy.getConstructor(new Class[] { ").append(
						Key.class.getName()).append(
						".class }).newInstance(new Object[] { ").append(
						DependencyConstants.keyFieldName(field.getName()))
						.append("});");
				code.append("} return ").append(fn).append(";}");
				CtMethod locator = CtNewMethod.make(code.toString(), pojo);
				pojo.addMethod(locator);
			}
		}
	}

	private void collectAnnotatedFields() throws ClassNotFoundException {

		/*
		 * TODO we should also collect fields in inner classes so we can
		 * instrument those as well
		 */

		/*
		 * TODO no need to look into fields in super classes because they will
		 * be instrumented as well?
		 */
		for (CtField field : pojo.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				if (findDependencyAnnot(field) != null) {
					annotatedFields.add(field);
				}
			}
		}
	}

	private Dependency findDependencyAnnot(CtField field)
			throws ClassNotFoundException {
		Object[] annots = field.getAvailableAnnotations();
		for (Object annot : annots) {
			if (annot instanceof Dependency) {
				return (Dependency) annot;
			}
		}
		return null;
	}

	private static final String locatorMethodName(CtField field) {
		return locatorMethodName(field.getName());
	}

	private static String locatorMethodName(String fieldName) {
		return LOCATOR_METHOD_PREFIX + fieldName;
	}

	private class FieldAccessEditor extends ExprEditor {
		private final Set<String> seenReads = new HashSet<String>();

		public FieldAccessEditor() {
			super();
		}

		@Override
		public void edit(FieldAccess f) throws CannotCompileException {
			final String fieldName = f.getFieldName();
			Dependency dep;
			try {
				dep = findDependencyAnnot(f.getField());

				if (dep != null) {
					switch (dep.strategy()) {
					case REMOVE_FIELD:
						instrumentRemoveField(f);
						break;
					case INJECT_FIELD:
						instrumentInjectField(f);
						break;
					}
					seenReads.add(fieldName);
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Error while instrumenting access to field "
								+ f.getFieldName(), e);
			}

		}

		private void instrumentInjectField(FieldAccess f)
				throws CannotCompileException, NotFoundException {
			if (f.isReader()) {
				f.replace("$_=$0." + locatorMethodName(f.getFieldName())
						+ "();");
			} else {
				// disable field writes to an injected field
				// TODO this might require more thinking through/configuration?
				// allow field write/disable it/throw exception?
				f.replace("");
			}
		}

		private void instrumentRemoveField(FieldAccess f)
				throws CannotCompileException, NotFoundException {
			if (f.isReader()) {

				final String localName = "__local" + f.getFieldName();
				if (!seenReads.contains(f.getFieldName())) {

					f.where().addLocalVariable(localName,
							f.getField().getType());

					String src = localName
							+ "=("
							+ f.getField().getType().getName()
							+ ")"
							+ LOCATOR_CALL
							+ "("
							+ pojo.getName()
							+ "."
							+ DependencyConstants
									.keyFieldName(f.getFieldName()) + ");";

					src += "$_=" + localName + ";";

					f.replace(src);
				} else {
					f.replace("$_=" + localName + ";");
				}
			} else {
				// completely remove write access since the field will be gone,
				// TODO maybe if we are not in a setter/constructor we should
				// throw an exception
				f.replace("");
			}
		}
	}

}
