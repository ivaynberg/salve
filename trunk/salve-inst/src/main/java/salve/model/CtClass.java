package salve.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CtClass {
	private static String getMethodIdentity(String name, String desc) {
		return name + "|" + desc;
	}

	private final CtProject project;
	private final int access;
	private final String name;
	private final String signature;
	private final String superClass;
	private final String[] interfaces;
	private final Map<String, CtField> fields = new LinkedHashMap<String, CtField>();
	private final Map<String, CtMethod> methods = new LinkedHashMap<String, CtMethod>();
	private final Map<String, CtAnnotation> annots = new LinkedHashMap<String, CtAnnotation>();
	private final List<CtInnerClass> inner = new ArrayList<CtInnerClass>(0);
	private CtOuterClass outer = null;

	public CtClass(CtProject project, int access, String name, String signature, String superClass,
			String[] interfaces) {
		this.project = project;
		this.access = access;
		this.name = name;
		this.signature = signature;
		this.superClass = superClass;
		this.interfaces = interfaces;
	}

	void add(CtAnnotation am) {
		annots.put(am.getDesc(), am);
	}

	void add(CtField fm) {
		fields.put(fm.getName(), fm);
	}

	void add(CtInnerClass icm) {
		inner.add(icm);
	}

	void add(CtMethod mm) {
		methods.put(getMethodIdentity(mm.getName(), mm.getDesc()), mm);
	}

	public int getAccess() {
		return access;
	}

	public CtAnnotation getAnnotation(String desc) {
		return annots.get(desc);
	}

	public CtField getField(String name) {
		return fields.get(name);
	}

	public String[] getInterfaceNames() {
		return interfaces;
	}

	public CtMethod getMethod(String name, String desc) {
		return methods.get(getMethodIdentity(name, desc));
	}

	public Collection<CtMethod> getMethods() {
		return methods.values();
	}

	public String getName() {
		return name;
	}

	public CtProject getProject() {
		return project;
	}

	public String getSignature() {
		return signature;
	}

	public CtClass getSuperClass() {
		if (superClass == null) {
			return null;
		}
		return project.getClass(superClass);
	}

	public String getSuperClassName() {
		return superClass;
	}

	CtClass setOuter(CtOuterClass model) {
		this.outer = model;
		return this;
	}

}
