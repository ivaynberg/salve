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
package salve.depend.impl;

import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;

class ClinitMerger extends MethodAdapter implements Opcodes {

	public ClinitMerger(MethodVisitor mv) {
		super(mv);
	}

	@Override public void visitCode() {
	}

	@Override public void visitEnd() {
	}

	@Override public void visitInsn(int opcode) {
		if (opcode != RETURN) {
			mv.visitInsn(opcode);
		}
	}

	@Override public void visitMaxs(int maxStack, int maxLocals) {

	}
}
