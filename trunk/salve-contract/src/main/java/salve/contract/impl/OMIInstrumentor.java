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

import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;

public class OMIInstrumentor extends ClassAdapter {
	private String owner;
	private final OMIAnalyzer analyzer;

	public OMIInstrumentor(ClassVisitor cv, OMIAnalyzer analyzer) {
		super(cv);
		this.analyzer = analyzer;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		owner = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		if (analyzer.shouldInstrument(name, desc)) {
			return new MethodInstrumentor(mv, access, name, desc);
		}
		return mv;
	}

	private class MethodInstrumentor extends AbstractMethodInstrumentor {
		private int flag;

		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
			if (isSuperCall(opcode, owner, name, desc)) {
				push(true);
				storeLocal(flag);
			}
			super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		protected void onMethodEnter() {
			flag = newLocal(Type.BOOLEAN_TYPE);
			push(false);
			storeLocal(flag);
		}

		@Override
		protected void onMethodExit(int opcode) {
			if (opcode != ATHROW) {
				Label ok = new Label();
				loadLocal(flag);
				ifZCmp(NE, ok);
				throwIllegalStateException("This method did not invoke super implementation before returning");
				mark(ok);
			}
		}

		private boolean isSuperCall(int opcode, String owner, String name,
				String desc) {
			return opcode == INVOKESPECIAL && getMethodName().equals(name)
					&& getMethodDesc().equals(desc)
					&& !OMIInstrumentor.this.owner.equals(owner);
		}

	}
}
