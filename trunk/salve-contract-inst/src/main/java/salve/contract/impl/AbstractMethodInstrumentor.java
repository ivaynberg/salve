/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.contract.impl;

import java.util.ArrayList;
import java.util.List;

import salve.CodeMarker;
import salve.InstrumentationContext;
import salve.asmlib.AdviceAdapter;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;

public abstract class AbstractMethodInstrumentor extends AdviceAdapter implements Constants {
	private final int access;
	private final String name;
	private final String desc;
	private final Type[] paramTypes;
	private final String[] paramNames;
	private final String owner;
	private final InstrumentationContext ctx;

	private CodeMarker marker;
	private CodeMarker first;
	private List<String> errors;

	public AbstractMethodInstrumentor(MethodVisitor mv, String owner, int access, String name, String desc,
			InstrumentationContext ctx) {
		super(mv, access, name, desc);
		this.access = access;
		this.name = name;
		this.desc = desc;
		paramTypes = Type.getArgumentTypes(desc);
		paramNames = new String[paramTypes.length];
		this.owner = owner;

		this.ctx = ctx;
	}

	protected void error(String error, Object... params) {
		if (errors == null) {
			errors = new ArrayList<String>(1);
		}
		errors.add(String.format(error, params));
	}

	public InstrumentationContext getContext() {
		return ctx;
	}

	public CodeMarker getCurrentMarker() {
		return marker;
	}

	protected int getMethodAccess() {
		return access;
	}

	protected String getMethodDefinitionString() {
		return getMethodDefinitionString(null);
	}

	protected String getMethodDefinitionString(String owner) {
		StringBuilder b = new StringBuilder();
		if (owner != null) {
			b.append(owner).append(".");
		}
		b.append(name).append(desc);
		return b.toString();
	}

	protected String getMethodDesc() {
		return desc;
	}

	protected String getMethodName() {
		return name;
	}

	public String getOwner() {
		return owner;
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

	@Override
	public void visitEnd() {
		if (errors != null) {
			for (String error : errors) {
				ctx.getLogger().error(first, error);
			}
		}
		super.visitEnd();
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		marker = new CodeMarker(owner, line);
		if (first == null) {
			first = marker;
		}
		super.visitLineNumber(line, start);
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		int pindex = index - 1;
		if (pindex >= 0 && pindex < paramNames.length) {
			paramNames[pindex] = name;
		}
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

}
