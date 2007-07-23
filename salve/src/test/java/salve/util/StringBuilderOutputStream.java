package salve.util;

import java.io.IOException;
import java.io.OutputStream;

public class StringBuilderOutputStream extends OutputStream {
	private final StringBuilder builder;

	public StringBuilderOutputStream() {
		builder = new StringBuilder();
	}

	public StringBuilderOutputStream(StringBuilder builder) {
		super();
		this.builder = builder;
	}

	public StringBuilder getBuilder() {
		return builder;
	}

	@Override
	public void write(int b) throws IOException {
		builder.append((char) b);
	}

}
