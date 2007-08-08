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
package salve.depend;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Attribute;
import salve.asmlib.FieldVisitor;

/**
 * Field visitor that strips any {@link Dependency} annotations
 * 
 * @author ivaynberg
 * 
 */
class DependencyAnnotRemover implements FieldVisitor {
	private final FieldVisitor delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate
	 *            field visitor to delegate to
	 */
	public DependencyAnnotRemover(FieldVisitor delegate) {
		super();
		this.delegate = delegate;
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (Constants.DEP_DESC.equals(desc)) {
			return null;
		} else {
			return delegate.visitAnnotation(desc, visible);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitAttribute(Attribute attr) {
		delegate.visitAttribute(attr);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitEnd() {
		delegate.visitEnd();
	}

}
