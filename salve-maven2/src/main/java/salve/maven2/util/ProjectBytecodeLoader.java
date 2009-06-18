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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.FilePathLoader;

import java.io.File;
import java.util.Set;

public class ProjectBytecodeLoader extends CompoundLoader {

	@SuppressWarnings("unchecked")
	public ProjectBytecodeLoader(MavenProject project, final boolean includeTestClasses)
			throws DependencyResolutionRequiredException {

		// add target/classes folder
		File dir = new File(project.getBuild().getOutputDirectory());
		if (dir.exists()) {
			addLoader(new FilePathLoader(dir));
		}

		// add target/test-classes folder (production classes must not refer to test-classes, but vice-versa is ok)
		if(includeTestClasses) {
			dir = new File(project.getBuild().getTestOutputDirectory());
			if (dir.exists()) {
				addLoader(new FilePathLoader(dir));
			}
		}

		// append project class path entries
		for (Object path : project.getCompileClasspathElements()) {
			dir = new File((String) path);
			if (dir.exists()) {
				addLoader(new FilePathLoader(dir));
			}
		}

		// append test class path entries if running instrumentation of test classes
		if(includeTestClasses) {
			for (Object path : project.getTestClasspathElements()) {
				dir = new File((String) path);
				if (dir.exists()) {
					addLoader(new FilePathLoader(dir));
				}
			}
		}

		// merge provided artifacts into the classpath because instrumentor jars
		// should be scoped provided
		Set<Artifact> artifacts = project.getDependencyArtifacts();
		for (Artifact artifact : artifacts) {
			if ("provided".equalsIgnoreCase(artifact.getScope())) {
				dir = artifact.getFile();
				if (dir != null && dir.exists()) {
					addLoader(new FilePathLoader(dir));
				}
			}
		}

		// append system classpath
		ClassLoader system = Object.class.getClassLoader();
		if (system == null) {
			system = Thread.currentThread().getContextClassLoader();
		}
		addLoader(new ClassLoaderLoader(system));
	}

}
