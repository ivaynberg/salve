package salve.contract.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.AdviceAdapter;

public abstract class AbstractMethodInstrumentor extends AdviceAdapter
		implements Constants {
	private final int methodAccess;
	private final String methodName;
	private final String methodDesc;
	private List<Parameter> params;

	public AbstractMethodInstrumentor(MethodVisitor mv, int access,
			String name, String desc) {
		super(mv, access, name, desc);
		methodAccess = access;
		methodName = name;
		methodDesc = desc;
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		Parameter param = getParameter(index - 1);
		if (param != null) {
			param.name = name;
			param.desc = desc;
		}
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	protected String getMethodDefinitionString() {
		// TODO better method def representation
		return methodName + methodDesc;
	}

	protected Parameter getOrCreateParameter(int index) {
		Parameter param = getParameter(index);
		if (param == null) {
			param = new Parameter();
			param.index = index;
			if (params == null) {
				initParametersList();
			}
			params.add(param);
		}
		return param;
	}

	/**
	 * @param index
	 */
	protected Parameter getParameter(int index) {
		if (params == null) {
			return null;
		}
		for (Parameter param : params) {
			if (param.index == index) {
				return param;
			}
		}
		return null;
	}

	protected List<Parameter> getParameters() {
		if (params == null) {
			return Collections.emptyList();
		} else {
			return params;
		}
	}

	/**
	 * @param param
	 */
	protected void throwIllegalArgumentException(Parameter param) {
		String msg = "Argument ";
		if (param.name != null) {
			msg += "`" + param.name + "`";
		} else {
			msg += "at index " + param.index;
			if (param.desc != null) {
				final String argType = Type.getType(param.desc).getClassName();
				msg += " and type " + argType;
			}
		}
		throwIllegalArgumentException(msg);
	}

	/**
	 * @param msg
	 */
	protected void throwIllegalArgumentException(String msg) {
		newInstance(ILLEGALARGEX);
		dup();
		push(msg);
		invokeConstructor(ILLEGALARGEX, ILLEGALARGEX_INIT);
		throwException();
	}

	/**
	 * @param msg
	 */
	protected void throwIllegalStateException(String msg) {
		newInstance(ILLEGALSTATEEX);
		dup();
		push(msg);
		invokeConstructor(ILLEGALSTATEEX, ILLEGALSTATEEX_INIT);
		throwException();
	}

	private void initParametersList() {
		params = new ArrayList<Parameter>(4);
	}

	protected static class Parameter {
		public int index;
		public String name;
		public String desc;
	}

}
