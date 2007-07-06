package salve.maven2;

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

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import salve.bytecode.PojoInstrumentor;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal instrument
 * 
 */
public class SalveMojo extends AbstractMojo {
	private static final FileFilter CLASS_FILTER = new ExtensionFileFilter(
			".class");

	/**
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		final File classes = new File(project.getBuild().getOutputDirectory());

		if (!classes.exists()) {
			throw new IllegalStateException(
					"target/classes directory does not exist");
		}

		final ClassPool pool = new ClassPool(ClassPool.getDefault());
		try {
			for (Object path : project.getCompileClasspathElements()) {
				pool.appendClassPath((String) path);
			}
			pool.appendClassPath(classes.getAbsolutePath());
		} catch (NotFoundException e) {
			// TODO better exception
			throw new RuntimeException("Could not setup javassist classpath", e);
		} catch (DependencyResolutionRequiredException e) {
			// TODO better exception
			throw new RuntimeException("Could not setup javassist classpath", e);
		}

		try {
			CtClass clazz = pool.get("javax.persistence.GeneratedValue");
			int a = 2;
			int b = a + 2;
		} catch (NotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Directory dir = new Directory(classes);
		dir.visitFiles(new FilteredFileVisitor(CLASS_FILTER) {

			@Override
			protected void onAcceptedFile(File file) {
				String classname = file.getAbsolutePath();
				classname = classname.substring(classes.getAbsolutePath()
						.length() + 1, classname.length() - 6);
				classname = classname.replace(File.separatorChar, '.');

				System.out.println("INSTRUMENTING: " + classname);

				try {
					CtClass clazz = pool.get(classname);
					PojoInstrumentor inst = new PojoInstrumentor(clazz);
					byte[] bytecode = inst.instrument().toBytecode();
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(bytecode);
					fos.flush();
					fos.close();
				} catch (Exception e) {
					// TODO better exception
					throw new RuntimeException("Could not instrument "
							+ classname, e);
				}
			}

		});

	}
}
