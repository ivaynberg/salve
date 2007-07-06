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
package salve.maven2;

import javassist.ClassPool;

import org.apache.maven.project.MavenProject;

/**
 * Javassist {@link ClassPool} with classpath based on the specified maven
 * project
 * 
 * @author ivaynberg
 */
public class ProjectClassPool extends ClassPool {

	/**
	 * Constructor
	 * 
	 * @param project
	 *            maven project to base the classpath on
	 */
	public ProjectClassPool(MavenProject project) {
		super(ClassPool.getDefault());
		try {
			// append project jars
			for (Object path : project.getCompileClasspathElements()) {
				appendClassPath((String) path);
			}
			// append target/classes folder
			appendClassPath(project.getBuild().getOutputDirectory());
		} catch (Exception e) {
			throw new RuntimeException("Could not setup classpath", e);
		}

	}

}
