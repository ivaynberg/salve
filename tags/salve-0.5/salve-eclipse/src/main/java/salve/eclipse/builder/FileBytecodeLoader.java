package salve.eclipse.builder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;

public class FileBytecodeLoader implements BytecodeLoader {
	private final IFile file;

	public FileBytecodeLoader(IFile file) {
		if (file == null) {
			throw new IllegalArgumentException("Argument `file` cannot be null");
		}
		this.file = file;
	}

	public byte[] loadBytecode(String className) {
		try {
			InputStream in = file.getContents();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			while (true) {
				int read = in.read(buff);
				if (read < 1) {
					break;
				}
				out.write(buff, 0, read);
			}
			return out.toByteArray();
		} catch (Exception e) {
			throw new CannotLoadBytecodeException(className, e);
		}
	}
}
