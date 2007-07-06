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
 * File filter that filters based on file extension
 * 
 * @author ivaynberg
 * 
 */
public class ExtensionFileFilter implements FileFilter {
	private final String extension;

	/**
	 * Constructor
	 * 
	 * @param extension
	 *            extension to filter on
	 */
	public ExtensionFileFilter(String extension) {
		super();
		this.extension = extension;
		if (!extension.startsWith(".")) {
			extension = "." + extension;
		}
	}

	/**
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		return pathname.getName().endsWith(extension);
	}
}