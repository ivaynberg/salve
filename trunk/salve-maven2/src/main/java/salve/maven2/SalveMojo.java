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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javassist.ClassPool;
import javassist.CtClass;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import salve.Instrumentor;
import salve.config.Config;
import salve.config.ConfigException;
import salve.config.PackageConfig;
import salve.config.XmlConfigReader;
import salve.maven2.util.ClassFileVisitor;
import salve.maven2.util.Directory;
import salve.maven2.util.ProjectClassLoader;
import salve.maven2.util.ProjectClassPool;

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
	private final Config config = new Config();
	private ClassPool pool;
	private ClassLoader loader;

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
		pool = new ProjectClassPool(project);
		loader = new ProjectClassLoader(project);

		final File classes = new File(project.getBuild().getOutputDirectory());

		// make sure target/classes has been created
		if (!classes.exists()) {
			throw new IllegalStateException(
					"target/classes directory does not exist");
		}

		loadConfig(classes);

		// visit class files and instrument them
		Directory dir = new Directory(classes);
		dir.visitFiles(new ClassFileVisitor(dir) {

			@Override
			protected void onClassFile(File file, String className) {
				instrumentClassFile(className, file);
			}
		});

		getLog().info(
				String.format("Salve: classes scanned: %d, instrumented: %d",
						scanned, instrumented));

	}

	/**
	 * Instruments the specified class file
	 * 
	 * @param className
	 * @param file
	 */
	private void instrumentClassFile(String className, File file) {

		getLog().debug("Scanning " + className);
		scanned++;

		try {
			PackageConfig conf = config.getPackageConfig(className);
			if (conf != null) {
				for (Instrumentor inst : conf.getInstrumentors()) {
					getLog().debug("Instrumenting " + className);
					CtClass clazz = pool.get(className);
					byte[] bytecode = inst.instrument(loader, className, clazz
							.toBytecode());
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(bytecode);
					fos.close();
					instrumented++;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not instrument " + className, e);
		}
	}

	/**
	 * @param classes
	 * @throws MojoExecutionException
	 */
	private void loadConfig(final File classes) throws MojoExecutionException {

		final File salvexml = new File(classes, "META-INF" + File.separator
				+ "salve.xml");
		if (!salvexml.exists()) {
			throw new MojoExecutionException(
					"Could not locate salve config file: " + salvexml);
		}

		XmlConfigReader reader = new XmlConfigReader(new ProjectClassLoader(
				project));
		try {
			reader.read(new FileInputStream(salvexml), config);
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Could not open " + salvexml
					+ " for reading");
		} catch (ConfigException e) {
			throw new MojoExecutionException(
					"Could not configure salve instrumentation", e);
		}
	}

}
