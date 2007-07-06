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
 * File visitor that filters files it visits using {@link FileFilter}
 * 
 * @author ivaynberg
 * 
 */
public abstract class FilteredFileVisitor implements FileVisitor {
	private final FileFilter filter;

	/**
	 * Constructor
	 * 
	 * @param filter
	 *            file filter to use
	 */
	public FilteredFileVisitor(FileFilter filter) {
		super();
		this.filter = filter;
	}

	/**
	 * @see salve.maven2.FileVisitor#onFile(java.io.File)
	 */
	public final void onFile(File file) {
		if (filter.accept(file)) {
			onAcceptedFile(file);
		}
	}

	/**
	 * Called when a file that passed the filter is visited
	 * 
	 * @param file
	 *            visited file
	 */
	protected abstract void onAcceptedFile(File file);

}
