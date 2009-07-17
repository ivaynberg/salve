package salve.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import salve.asmlib.ClassReader;
import salve.asmlib.Type;

public class MethodModel {
	public static class ParameterAnnotations {
		private final Map<String, AnnotationModel> annots;

		public ParameterAnnotations() {
			annots = new LinkedHashMap<String, AnnotationModel>();
		}

		public Collection<AnnotationModel> getAnnotations() {
			return annots.values();
		}

	}

	private final ClassModel clazz;
	private final int access;
	private final String name;
	private final String desc;
	private final String signature;
	private final String[] exceptions;
	private final Map<String, AnnotationModel> annots = new LinkedHashMap<String, AnnotationModel>();

	private MethodModel superMethod;
	private boolean superMethodCached = false;

	private ParameterAnnotations[] parameterAnnots;

	private static final int META_VISITOR = ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;

	public MethodModel(ClassModel clazz, int access, String name, String desc, String signature, String[] exceptions) {
		this.clazz = clazz;
		this.access = access;
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		this.exceptions = exceptions;
	}

	void add(AnnotationModel am) {
		annots.put(am.getDesc(), am);
	}

	void addParameterAnnot(int parameter, AnnotationModel am) {
		if (parameterAnnots == null) {
			parameterAnnots = new ParameterAnnotations[Type.getArgumentTypes(desc).length];
		}
		if (parameterAnnots[parameter] == null) {
			parameterAnnots[parameter] = new ParameterAnnotations();
		}
		parameterAnnots[parameter].annots.put(am.getDesc(), am);
	}

	private MethodModel findSuper(String cn, MethodModel target) {
		Method t = new Method(target.getAccess(), target.getName(), target.getDesc());

		ClassModel cursor = clazz;

		while (cursor.getSuperClass() != null) {
			cursor = cursor.getProject().getClass(cursor.getSuperClass());
			for (MethodModel mm : cursor.getMethods()) {
				Method m = new Method(mm.getAccess(), mm.getName(), mm.getDesc());
				if (m.canOverride(t)) {
					return mm;
				}
			}
		}
		return null;
	}

	public int getAccess() {
		return access;
	}

	public AnnotationModel getAnnotation(String desc) {
		return annots.get(desc);
	}

	public Collection<AnnotationModel> getAnnotations() {
		return annots.values();
	}

	public ClassModel getClassModel() {
		return clazz;
	}

	public String getDesc() {
		return desc;
	}

	public String[] getExceptions() {
		return exceptions;
	}

	public String getName() {
		return name;
	}

	public ParameterAnnotations[] getParameterAnnots() {
		return parameterAnnots;
	}

	public String getSignature() {
		return signature;
	}

	public MethodModel getSuper() {
		if (!superMethodCached) {
			if (clazz.getSuperClass() != null) {
				superMethod = findSuper(clazz.getSuperClass(), this);
			}
			superMethodCached = true;
		}
		return superMethod;
	}

}
