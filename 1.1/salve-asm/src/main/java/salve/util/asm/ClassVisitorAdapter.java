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

/**
 * Simple adapter for class visitors
 * 
 * @author ivaynberg
 * 
 */
public class ClassVisitorAdapter implements ClassVisitor {

	/**
	 * {@inheritDoc}
	 */
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return new AnnotationVisitorAdapter();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAttribute(Attribute attr) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {

	}

	/**
	 * {@inheritDoc}
	 */
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return new FieldVisitorAdapter();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitInnerClass(String name, String outerName, String innerName, int access) {

	}

	/**
	 * {@inheritDoc}
	 */
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return new MethodVisitorAdapter();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitOuterClass(String owner, String name, String desc) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitSource(String source, String debug) {
	}

}
