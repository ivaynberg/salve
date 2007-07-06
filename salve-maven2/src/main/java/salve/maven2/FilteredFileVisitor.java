package salve.maven2;

import java.io.File;
import java.io.FileFilter;

public abstract class FilteredFileVisitor implements FileVisitor {
	private final FileFilter filter;

	public FilteredFileVisitor(FileFilter filter) {
		super();
		this.filter = filter;
	}

	public final void onFile(File file) {
		if (filter.accept(file)) {
			onAcceptedFile(file);
		}
	}

	protected abstract void onAcceptedFile(File file);

}
