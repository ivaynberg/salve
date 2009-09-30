package salve.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import salve.asmlib.ClassReader;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;

public class CtMethod {
	public static class ParameterAnnotations {
		private final Map<String, CtAnnotation> annots;

		public ParameterAnnotations() {
			annots = new LinkedHashMap<String, CtAnnotation>();
		}
	}

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private final CtClass clazz;
	private final int access;
	private final String signature;
	private final String[] exceptions;
	private final Map<String, CtAnnotation> annots = new LinkedHashMap<String, CtAnnotation>();
	private final String[] argNames;
	private CtMethod superMethod;
	private final boolean superMethodCached = false;
	private final salve.asmlib.Method method;
	private ParameterAnnotations[] parameterAnnots;

	private static final int META_VISITOR = ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;

	public CtMethod(CtClass clazz, int access, String name, String desc, String signature, String[] exceptions) {
		this.clazz = clazz;
		this.access = access;
		this.signature = signature;
		this.exceptions = exceptions;
		this.method = new salve.asmlib.Method(name, desc);
		argNames = new String[method.getArgumentTypes().length];
	}

	void add(CtAnnotation am) {
		annots.put(am.getDesc(), am);
	}

	void addParameterAnnot(int parameter, CtAnnotation am) {
		if (parameterAnnots == null) {
			parameterAnnots = new ParameterAnnotations[method.getArgumentTypes().length];
		}
		if (parameterAnnots[parameter] == null) {
			parameterAnnots[parameter] = new ParameterAnnotations();
		}
		parameterAnnots[parameter].annots.put(am.getDesc(), am);
	}

	private CtMethod findSuper(String cn, CtMethod target) {
		Method t = new Method(target.getAccess(), target.getName(), target.getDesc());

		CtClass cursor = clazz;

		while (cursor.getSuperClassName() != null) {
			final String sn = cursor.getSuperClassName();
			cursor = cursor.getProject().getClass(sn);
			// if (cursor == null) {
			// System.out.println(sn);
			// System.out.println(sn.length());
			// }
			for (CtMethod mm : cursor.getMethods()) {
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

	public CtAnnotation getAnnot(String desc) {
		return annots.get(desc);
	}

	public Collection<CtAnnotation> getAnnotations() {
		return annots.values();
	}

	public CtAnnotation getArgAnnot(int idx, String desc) {
		if (parameterAnnots != null) {
			if (parameterAnnots[idx] != null) {
				return parameterAnnots[idx].annots.get(desc);
			}
		}

		return annots.get(desc);
	}

	public Collection<CtAnnotation> getArgAnnots(int idx) {
		if (parameterAnnots != null) {
			if (parameterAnnots[idx] != null) {
				return parameterAnnots[idx].annots.values();
			}
		}
		return Collections.emptyList();
	}

	public int getArgCount() {
		return method.getArgumentTypes().length;
	}

	public CtClass getClassModel() {
		return clazz;
	}

	public String getDesc() {
		return method.getDescriptor();
	}

	public String[] getExceptions() {
		if (exceptions != null) {
			return exceptions;
		} else {
			return EMPTY_STRING_ARRAY;
		}
	}

	public String getName() {
		return method.getName();
	}

	public Type getReturnType() {
		return method.getReturnType();
	}

	public String getSignature() {
		return signature;
	}

	// public MethodModel getSuper() {
	// if (!superMethodCached) {
	// if (clazz.getSuperClassName() != null) {
	// superMethod = findSuper(clazz.getSuperClassName(), this);
	// }
	// superMethodCached = true;
	//
	// ClassModel parent = clazz.getSuperClass();
	// while (parent != null) {
	// clazz.getProject().register(parent.getName(), new UpdateListener() {
	//
	// public Action updated() {
	// superMethodCached = false;
	// superMethod = null;
	// return Action.REMOVE;
	// }
	//
	// });
	// parent = parent.getSuperClass();
	// }
	//
	// }
	// return superMethod;
	// }

	// public ParameterAnnotations[] getParameterAnnots() {
	// return parameterAnnots;
	// }

	public boolean hasArgAnnot(int idx, String desc) {
		return getArgAnnot(idx, desc) != null;
	}

	public boolean hasArgAnnot(int idx, Type type) {
		return hasArgAnnot(idx, type.getDescriptor());
	}

	public boolean isStatic() {
		return (access & Opcodes.ACC_STATIC) > 0;
	}

	void setArgName(int idx, String name) {
		argNames[idx] = name;
	}

	@Override
	public String toString() {
		return clazz.getName() + "#" + method.getName();
	}
}