package salve.maven2;

import java.io.File;
import java.io.FileFilter;

public class ExtensionFileFilter implements FileFilter {
	private final String extension;

	public ExtensionFileFilter(String extension) {
		super();
		this.extension = extension;
		if (!extension.startsWith(".")) {
			extension = "." + extension;
		}
	}

	public boolean accept(File pathname) {
		return pathname.getName().endsWith(extension);
	}
}
