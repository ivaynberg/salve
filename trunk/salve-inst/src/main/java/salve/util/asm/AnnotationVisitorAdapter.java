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

/**
 * A simple adapter for AnnotationVisitor interface
 * 
 * @author ivaynberg
 */
public class AnnotationVisitorAdapter implements AnnotationVisitor {

	/**
	 * {@inheritDoc}
	 */
	public void visit(String name, Object value) {

	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitArray(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnum(String name, String desc, String value) {
	}

}
