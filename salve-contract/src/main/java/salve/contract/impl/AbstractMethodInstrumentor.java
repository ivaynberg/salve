package salve.contract.impl;

import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.AdviceAdapter;

public abstract class AbstractMethodInstrumentor extends AdviceAdapter
		implements Constants {
	private final int access;
	private final String name;
	private final String desc;
	private final Type[] paramTypes;
	private final String[] paramNames;

	public AbstractMethodInstrumentor(MethodVisitor mv, int access,
			String name, String desc) {
		super(mv, access, name, desc);
		this.access = access;
		this.name = name;
		this.desc = desc;
		paramTypes = Type.getArgumentTypes(desc);
		paramNames = new String[paramTypes.length];
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		int pindex = index - 1;
		if (pindex >= 0 && pindex < paramNames.length) {
			paramNames[pindex] = name;
		}
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	protected String getMethodDefinitionString() {
		return name + desc;
	}

	protected String getMethodDesc() {
		return desc;
	}

	protected String getMethodName() {
		return name;
	}

	protected int getParamCount() {
		return paramTypes.length;
	}

	protected String getParamName(int index) {
		return paramNames[index];
	}

	protected Type getParamType(int index) {
		return paramTypes[index];
	}

	protected Type getReturnType() {
		return Type.getReturnType(desc);
	}

	/**
	 * @param param
	 */
	protected void throwIllegalArgumentException(int param, String message) {
		String msg = "Argument ";
		if (getParamName(param) != null) {
			msg += "`" + getParamName(param) + "`";
		} else {
			msg += "at index " + param;
			msg += " and type " + getParamType(param).getClassName();
		}
		msg += " " + message;
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

}
