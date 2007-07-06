package salve.maven2;

import java.io.File;

public class Directory {
	private final File dir;

	public Directory(File dir) {
		if (!dir.exists()) {
			throw new IllegalArgumentException(
					"Argument `dir` points to a location that does not exist");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(
					"Argument `dir` is not a directory");
		}
		this.dir = dir;
	}

	public void visitFiles(FileVisitor visitor) {
		visitFiles(dir, visitor);
	}

	private void visitFiles(File dir, FileVisitor visitor) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				visitFiles(file, visitor);
			} else {
				visitor.onFile(file);
			}
		}
	}
}
