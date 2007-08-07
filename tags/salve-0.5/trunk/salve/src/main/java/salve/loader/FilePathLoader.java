package salve.loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FilePathLoader extends AbstractUrlLoader {
	private final File path;

	public FilePathLoader(File path) {
		if (path == null) {
			throw new IllegalArgumentException("Argument `path` cannot be null");
		}
		this.path = path;
	}

	@Override
	protected URL getBytecodeUrl(String className) {
		final String pathName = path.getAbsolutePath();
		final String fileName = className + ".class";
		if (pathName.endsWith(".jar") || pathName.endsWith(".zip")) {
			return findResourceInJar(path, fileName);
		} else {
			return findResourceInDir(path, fileName);
		}
	}

	private static URL findResourceInDir(File path, String name) {
		File file = new File(path.getAbsolutePath() + File.separator + name);
		if (file.exists()) {
			try {
				return file.getCanonicalFile().toURL();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}

		return null;
	}

	private static URL findResourceInJar(File path, String name) {
		String jarUrl;
		try {
			JarFile jar = new JarFile(path);
			JarEntry je = jar.getJarEntry(name);
			if (je != null) {
				jarUrl = path.getCanonicalFile().toURL().toString();
				return new URL("jar:" + jarUrl + "!/" + name);
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}

		return null;
	}
}
