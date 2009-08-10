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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import salve.CodeMarker;
import salve.CodeMarkerAware;
import salve.ConfigException;
import salve.InstrumentationContext;
import salve.Instrumentor;
import salve.config.XmlConfig;
import salve.config.XmlConfigReader;
import salve.maven2.util.ClassFileVisitor;
import salve.maven2.util.Directory;
import salve.maven2.util.ProjectBytecodeLoader;
import salve.model.ProjectModel;
import salve.monitor.ModificationMonitor;
import salve.util.FallbackBytecodeClassLoader;

/**
 * Salve maven2 base mojo. This provides instrumentation for production and test
 * classes.
 */
public abstract class AbstractSalveMojo extends AbstractMojo {

	private int scanned = 0;
	private int instrumented = 0;
	private final XmlConfig config = new XmlConfig();
	private ProjectBytecodeLoader loader;
	private ProjectModel model;
	/**
	 * Maven project we are building
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * be verbose: print instrumented files on console
	 * 
	 * @parameter expression="${salve.maven2.verbose}"
	 */
	private final boolean verbose = false;

	/**
	 * show debug info: show which files are scanned and which instrumentors are
	 * applied
	 * 
	 * @parameter expression="${salve.maven2.debug}"
	 */
	private final boolean debug = false;

	/**
	 * instruments either production or test classes
	 * 
	 * @param instrumentTestClasses
	 *            <code>false</code> in order to instrument production classes,
	 *            <code>false</code> to instrument test classes
	 * @throws MojoExecutionException
	 *             when class file instrumentation did not succeed
	 */
	protected final void instrumentClasses(final boolean instrumentTestClasses) throws MojoExecutionException {
		final File classesDir = new File(project.getBuild().getOutputDirectory());

		// make sure target/classes has been created
		if (!classesDir.exists()) {
			throw new IllegalStateException("target/classes directory does not exist");
		}

		try {
			loader = new ProjectBytecodeLoader(project, instrumentTestClasses);
		} catch (DependencyResolutionRequiredException e) {
			throw new MojoExecutionException("Could not configure bytecode loader", e);
		}

		model = new ProjectModel().setLoader(loader);

		loadConfig(classesDir);

		if (instrumentTestClasses) {
			// instrument test classes (if any exist)
			final File testClassesDir = new File(project.getBuild().getTestOutputDirectory());
			if (testClassesDir.exists()) {
				instrumentClassFileDirectory(testClassesDir);
			}
		} else {
			// instrument production classes
			instrumentClassFileDirectory(classesDir);
		}

		getLog().info(String.format("Salve: classes scanned: %d, instrumented: %d", scanned, instrumented));
	}

	/**
	 * Instruments the specified class file
	 * 
	 * @param className
	 *            name of the java class
	 * @param file
	 *            file to instrument
	 */
	private void instrumentClassFile(String className, File file) {

		if (debug)
			getLog().info("Scanning " + className);
		scanned++;

		final String binClassName = className.replace('.', '/');

		try {
			ModificationMonitor monitor = new ModificationMonitor();
			for (Instrumentor inst : config.getInstrumentors(binClassName)) {
				if (debug)
					getLog().info("Checking " + className + " to apply " + inst.getClass().getName());

				InstrumentationContext ctx = new InstrumentationContext(loader, monitor, config.getScope(inst), model);

				byte[] bytecode = inst.instrument(binClassName, ctx);
				final FileOutputStream fos = new FileOutputStream(file);
				try {
					fos.write(bytecode);
				} finally {
					fos.close();
				}
			}
			if (monitor.isModified()) {
				if (verbose)
					getLog().info("Instrumented " + className);
				instrumented++;
			}
		} catch (Exception e) {
			int line = 0;
			if (e instanceof CodeMarkerAware) {
				CodeMarker marker = ((CodeMarkerAware) e).getCodeMarker();
				if (marker != null) {
					line = Math.max(0, marker.getLineNumber());
				}
			}
			StringBuilder message = new StringBuilder();
			message.append("Could not instrument ").append(className).append(".");
			if (line > 0) {
				message.append(" Error on line: ").append(line);
			}
			throw new RuntimeException(message.toString(), e);
		}
	}

	private void instrumentClassFileDirectory(final File classesDir) {
		// visit class files and instrument them
		Directory dir = new Directory(classesDir);
		dir.visitFiles(new ClassFileVisitor(dir) {

			@Override
			protected void onClassFile(File file, String className) {
				instrumentClassFile(className, file);
			}
		});
	}

	/**
	 * @param classes
	 *            class files root directory
	 * @throws MojoExecutionException
	 *             when config loading has failed
	 */
	private void loadConfig(final File classes) throws MojoExecutionException {

		final File salvexml = new File(classes, "META-INF" + File.separator + "salve.xml");
		if (!salvexml.exists()) {
			throw new MojoExecutionException("Could not locate salve config file: " + salvexml);
		}

		XmlConfigReader reader = new XmlConfigReader(new FallbackBytecodeClassLoader(AbstractSalveMojo.class
				.getClassLoader(), loader));
		try {
			reader.read(new FileInputStream(salvexml), config);
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Could not open " + salvexml + " for reading");
		} catch (ConfigException e) {
			throw new MojoExecutionException("Could not configure salve instrumentation", e);
		}
	}
}
