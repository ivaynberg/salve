package salve.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassModel {
	private static String getMethodIdentity(String name, String desc) {
		return name + "|" + desc;
	}

	private final ProjectModel project;
	private final int access;
	private final String name;
	private final String signature;
	private final String superClass;
	private final String[] interfaces;
	private final Map<String, FieldModel> fields = new LinkedHashMap<String, FieldModel>();
	private final Map<String, MethodModel> methods = new LinkedHashMap<String, MethodModel>();
	private final Map<String, AnnotationModel> annots = new LinkedHashMap<String, AnnotationModel>();
	private final List<InnerClassModel> inner = new ArrayList<InnerClassModel>(0);
	private OuterClassModel outer = null;

	public ClassModel(ProjectModel project, int access, String name, String signature, String superClass,
			String[] interfaces) {
		this.project = project;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superClass = superClass;
		this.interfaces = interfaces;
	}

	void add(AnnotationModel am) {
		annots.put(am.getDesc(), am);
	}

	void add(FieldModel fm) {
		fields.put(fm.getName(), fm);
	}

	void add(InnerClassModel icm) {
		inner.add(icm);
	}

	void add(MethodModel mm) {
		methods.put(getMethodIdentity(mm.getName(), mm.getDesc()), mm);
	}

	public int getAccess() {
		return access;
	}

	public AnnotationModel getAnnotation(String desc) {
		return annots.get(desc);
	}

	public FieldModel getField(String name) {
		return fields.get(name);
	}

	public String[] getInterfaceNames() {
		return interfaces;
	}

	public MethodModel getMethod(String name, String desc) {
		return methods.get(getMethodIdentity(name, desc));
	}

	public Collection<MethodModel> getMethods() {
		return methods.values();
	}

	public String getName() {
		return name;
	}

	public ProjectModel getProject() {
		return project;
	}

	public String getSignature() {
		return signature;
	}

	public ClassModel getSuperClass() {
		return project.getClass(superClass);
	}

	public String getSuperClassName() {
		return superClass;
	}

	ClassModel setOuter(OuterClassModel model) {
		this.outer = model;
		return this;
	}

}
