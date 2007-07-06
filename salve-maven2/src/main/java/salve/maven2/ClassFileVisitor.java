/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.maven2;

import java.io.File;
import java.io.FileFilter;

/**
 * File visitor for visiting class files.
 * 
 * @author ivaynberg
 * 
 */
public abstract class ClassFileVisitor extends FilteredFileVisitor {

	private static final FileFilter CLASS_FILTER = new ExtensionFileFilter(
			".class");

	private final int prefixLength;

	/**
	 * Constructor
	 * 
	 * @param baseDir
	 *            directory that represents the default package
	 */
	public ClassFileVisitor(Directory baseDir) {
		super(CLASS_FILTER);
		prefixLength = baseDir.getAbsolutePath().length() + 1;
	}

	/**
	 * @see salve.maven2.FilteredFileVisitor#onAcceptedFile(java.io.File)
	 */
	@Override
	protected final void onAcceptedFile(File file) {
		String className = file.getAbsolutePath();
		className = className.substring(prefixLength, className.length() - 6);
		className = className.replace(File.separatorChar, '.');
		onClassFile(file, className);
	}

	/**
	 * Called when the visitor visits a class file
	 * 
	 * @param file
	 *            class file
	 * @param className
	 *            class name
	 */
	protected abstract void onClassFile(File file, String className);

}
