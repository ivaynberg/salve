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
package salve.maven2.util;

import java.io.File;

/**
 * Represents a file system directory
 * 
 * @author ivaynberg
 */
public class Directory {
	private final File dir;

	/**
	 * Constructor
	 * 
	 * @param dir
	 *            directory this object will represent
	 */
	public Directory(File dir) {
		if (!dir.exists()) {
			throw new IllegalArgumentException("Argument `dir` points to a location that does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Argument `dir` is not a directory");
		}
		this.dir = dir;
	}

	/**
	 * @return absolute path
	 */
	public String getAbsolutePath() {
		return dir.getAbsolutePath();
	}

	/**
	 * Visits all files in this directory and any of its subdirectories using
	 * the specified visitor
	 * 
	 * @param visitor
	 *            visitor to visit files with
	 */
	public void visitFiles(FileVisitor visitor) {
		visitFiles(dir, visitor);
	}

	private void visitFiles(File dir, FileVisitor visitor) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				visitFiles(file, visitor);
			} else {
				visitor.onFile(file);
			}
		}
	}
}
