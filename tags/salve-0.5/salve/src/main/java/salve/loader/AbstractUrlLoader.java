package salve.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import salve.BytecodeLoader;

public abstract class AbstractUrlLoader implements BytecodeLoader {

	public final byte[] loadBytecode(String className) {
		URL url = getBytecodeUrl(className);
		if (url == null) {
			return null;
		} else {
			try {
				final InputStream in = url.openStream();
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				final byte[] buff = new byte[1024];
				while (true) {
					int read = in.read(buff, 0, buff.length);
					if (read <= 0) {
						break;
					}
					out.write(buff, 0, read);
				}
				return out.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException("Could not read bytecode from " + url.toString(), e);
			}
		}
	}

	protected abstract URL getBytecodeUrl(String className);

}
