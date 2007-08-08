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
package salve.util.asm;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;

/**
 * Simple adapter for method visitors
 * 
 * @author ivaynberg
 * 
 */
public class MethodVisitorAdapter implements MethodVisitor {

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotationDefault() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAttribute(Attribute attr) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitCode() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitFrame(int type, int local, Object[] local2, int stack, Object[] stack2) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitIincInsn(int var, int increment) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitInsn(int opcode) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitIntInsn(int opcode, int operand) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitJumpInsn(int opcode, Label label) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLabel(Label label) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLdcInsn(Object cst) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLineNumber(int line, Label start) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMaxs(int maxStack, int maxLocals) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMultiANewArrayInsn(String desc, int dims) {

	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {

	}

	/**
	 * {@inheritDoc}
	 */

	public void visitTypeInsn(int opcode, String desc) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitVarInsn(int opcode, int var) {

	}

}
