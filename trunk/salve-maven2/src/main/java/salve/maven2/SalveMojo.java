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
import java.io.FileOutputStream;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import salve.bytecode.PojoInstrumentor;

/**
 * Salve maven2 plugin. This plugin instruments class files before the project
 * is packaged.
 * 
 * @goal instrument
 * @requiresDependencyResolution
 */
public class SalveMojo extends AbstractMojo {

	private int scanned = 0;
	private int instrumented = 0;

	/**
	 * Maven project we are building
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	/**
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException {
		final File classes = new File(project.getBuild().getOutputDirectory());

		// make sure target/classes has been created
		if (!classes.exists()) {
			throw new IllegalStateException(
					"target/classes directory does not exist");
		}

		final ClassPool pool = new ProjectClassPool(project);

		// visit class files and instrument them
		Directory dir = new Directory(classes);
		dir.visitFiles(new ClassFileVisitor(dir) {

			@Override
			protected void onClassFile(File file, String className) {
				instrumentClassFile(pool, file, className);
			}
		});

		getLog().info(
				String.format("Salve: classes scanned: %d, instrumented: %d",
						scanned, instrumented));

	}

	/**
	 * Instruments the specified class file
	 * 
	 * @param pool
	 * @param file
	 * @param className
	 */
	private void instrumentClassFile(final ClassPool pool, File file,
			String className) {

		getLog().debug("Scanning " + className);
		scanned++;

		try {
			CtClass clazz = pool.get(className);
			PojoInstrumentor inst = new PojoInstrumentor(clazz);
			boolean instrument = inst.instrument();
			if (instrument) {
				getLog().debug("Instrumenting " + className);
				byte[] bytecode = inst.getInstrumented().toBytecode();
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(bytecode);
				fos.close();
				instrumented++;
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not instrument " + className, e);
		}
	}

}
