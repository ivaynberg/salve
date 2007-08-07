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
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.FilePathLoader;

public class ProjectBytecodeLoader extends CompoundLoader {

	public ProjectBytecodeLoader(MavenProject project)
			throws DependencyResolutionRequiredException {

		// add target/classes folder
		addLoader(new FilePathLoader(new File(project.getBuild()
				.getOutputDirectory())));

		// append project class path entries
		for (Object path : project.getCompileClasspathElements()) {
			addLoader(new FilePathLoader(new File((String) path)));
		}

		// merge provided artifacts into the classpath because instrumentor jars
		// should be scoped provided
		Set<Artifact> artifacts = project.getDependencyArtifacts();
		for (Artifact artifact : artifacts) {
			if ("provided".equalsIgnoreCase(artifact.getScope())) {
				addLoader(new FilePathLoader(artifact.getFile()));
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
