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

import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;

/**
 * A patched version of {@link salve.asmlib.GeneratorAdapter}. This version
 * fixes {@link #push(Type)} when type is primitive.
 * 
 * see
 * http://forge.objectweb.org/tracker/?func=detail&aid=307378&group_id=23&atid=350023
 * 
 * @author ivaynberg
 * 
 */
public class GeneratorAdapter extends salve.asmlib.GeneratorAdapter implements Opcodes {
	/**
	 * Constructor
	 * 
	 * @param mv
	 *            method visitor
	 * @param access
	 *            method access flags
	 * @param name
	 *            method name
	 * @param desc
	 *            method descriptor
	 */
	public GeneratorAdapter(MethodVisitor mv, int access, String name, String desc) {
		super(mv, access, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(Type value) {
		if (value == null) {
			mv.visitInsn(Opcodes.ACONST_NULL);
		} else {
			int sort = value.getSort();
			if (sort == Type.BOOLEAN) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
			} else if (sort == Type.CHAR) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Char", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.BYTE) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.SHORT) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.INT) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.FLOAT) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.LONG) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
			} else

			if (sort == Type.DOUBLE) {
				mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
			} else {
				mv.visitLdcInsn(value);
			}

		}
	}
}
