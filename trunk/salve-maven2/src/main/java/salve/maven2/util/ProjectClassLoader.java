package salve.maven2.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

public class ProjectClassLoader extends ClassLoader {
	private final MavenProject project;
	private final ClassPool pool;

	public ProjectClassLoader(MavenProject project) {
		this.project = project;
		pool = new ProjectClassPool(project);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return pool.get(name).toClass();
		} catch (Exception e) {
			throw new ClassNotFoundException("Could not load class: " + name, e);
		}
	}

	@Override
	protected URL findResource(String name) {
		URL url = super.findResource(name);
		if (url == null) {
			try {
				url = findResourceInDir(
						project.getBuild().getOutputDirectory(), name);
				if (url == null) {
					for (Object pathObject : project
							.getCompileClasspathElements()) {
						final String path = (String) pathObject;
						if (path.endsWith(".jar") || path.endsWith(".zip")) {
							url = findResourceInJar(path, name);
						} else {
							url = findResourceInDir(path, name);
						}
						if (url != null) {
							break;
						}
					}
				}
			} catch (DependencyResolutionRequiredException e) {
				throw new RuntimeException(
						"Could not find resources because dependency resolution failed",
						e);
			}
		}
		return url;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		// TODO implement
		throw new IllegalStateException("THIS METHOD IS NOT YET IMPLEMENTED");
	}

	private URL findResourceInDir(String path, String name) {
		File file = new File(path + File.separator + name);
		if (file.exists()) {
			try {
				return file.getCanonicalFile().toURL();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}

		return null;
	}

	private URL findResourceInJar(String path, String name) {
		String jarUrl;
		try {
			jarUrl = new File(path).getCanonicalFile().toURL().toString();
			JarFile jar = new JarFile(path);
			JarEntry je = jar.getJarEntry(name);
			if (je != null) {
				return new URL("jar:" + jarUrl + "!/" + name);
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}

		return null;
	}
}
