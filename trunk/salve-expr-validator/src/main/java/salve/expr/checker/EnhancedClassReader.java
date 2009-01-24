package salve.expr.checker;

import java.io.IOException;
import java.io.InputStream;

import salve.asmlib.ClassReader;

public class EnhancedClassReader extends ClassReader {

	public EnhancedClassReader(byte[] b) {
		super(b);
	}

	public EnhancedClassReader(byte[] b, int off, int len) {
		super(b, off, len);
	}

	public EnhancedClassReader(InputStream is) throws IOException {
		super(is);
	}

	public EnhancedClassReader(String name) throws IOException {
		super(name);
	}

	public boolean isList() {
		if ("java/util/List".equals(getClassName())) {
			return true;
		} else {
			for (String iface : getInterfaces()) {
				if ("java/util/List".equals(iface)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isMap() {
		if ("java/util/Map".equals(getClassName())) {
			return true;
		} else {
			for (String iface : getInterfaces()) {
				if ("java/util/Map".equals(iface)) {
					return true;
				}
			}
		}
		return false;
	}
}
