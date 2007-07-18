package salve.asm.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import salve.asm.BytecodeLoader;

public class ClassLoaderLoader implements BytecodeLoader {
	private final ClassLoader loader;

	public ClassLoaderLoader(ClassLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException(
					"Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	public byte[] load(String className) {
		InputStream is = loader.getResourceAsStream(className + ".class");
		if (is == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int read = 0;
		try {
			while ((read = is.read(buff)) > 0) {
				baos.write(buff, 0, read);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO exception: nicer message
			throw new RuntimeException("Could not read bytecode", e);
		}
	}
}
