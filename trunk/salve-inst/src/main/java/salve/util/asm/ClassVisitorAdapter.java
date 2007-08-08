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
import salve.asmlib.ClassVisitor;
import salve.asmlib.FieldVisitor;
import salve.asmlib.MethodVisitor;

public class ClassVisitorAdapter implements ClassVisitor {

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	public void visitAttribute(Attribute attr) {

	}

	public void visitEnd() {

	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {

	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return null;
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	public void visitSource(String source, String debug) {
	}

}
