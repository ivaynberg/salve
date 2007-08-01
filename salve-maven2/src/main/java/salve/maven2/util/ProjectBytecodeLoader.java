package salve.maven2.util;

import java.io.File;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import salve.loader.ClassLoaderLoader;
import salve.loader.CompoundLoader;
import salve.loader.FilePathLoader;

public class ProjectBytecodeLoader extends CompoundLoader {

	public ProjectBytecodeLoader(MavenProject project) throws DependencyResolutionRequiredException {

		// add target/classes folder
		addLoader(new FilePathLoader(new File(project.getBuild().getOutputDirectory())));

		// append project jars
		for (Object path : project.getCompileClasspathElements()) {
			addLoader(new FilePathLoader(new File((String) path)));
		}

		// append system classpath
		ClassLoader system = Object.class.getClassLoader();
		if (system == null) {
			system = Thread.currentThread().getContextClassLoader();
		}
		addLoader(new ClassLoaderLoader(system));
	}

}
