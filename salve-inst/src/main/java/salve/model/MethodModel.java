package salve.model;

import java.util.LinkedHashMap;
import java.util.Map;

import salve.asmlib.Type;

public class MethodModel {
	public static class ParameterAnnotations {
		private final Map<String, AnnotationModel> annots = new LinkedHashMap<String, AnnotationModel>();

	}

	private final int acces;
	private final String name;
	private final String desc;
	private final String signature;
	private final String[] exceptions;
	private final Map<String, AnnotationModel> annots = new LinkedHashMap<String, AnnotationModel>();

	private ParameterAnnotations[] parameterAnnots;

	public MethodModel(int acces, String name, String desc, String signature, String[] exceptions) {
		this.acces = acces;
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
		parameterAnnots[parameter].annots.put(am.getDesc(), am);
	}

	public int getAcces() {
		return acces;
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

	public String getSignature() {
		return signature;
	}
}
