package salve.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

public class StreamsUtil {
	/**
	 * Closes a {@link Closeable#}. Wraps the {@link IOException} with a
	 * {@link RuntimeException}. Error message is constructed using
	 * {@link MessageFormat#format(String, Object...)}.
	 * 
	 * @param closeable
	 *            closeable to close
	 * @param errorMessage
	 *            message give to exception in case of an error
	 * @param errorParams
	 *            parameters used to construct the message
	 */
	public static void close(Closeable closeable, String errorMessage, Object... errorParams) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				throw new RuntimeException(message(errorMessage, errorParams), e);
			}
		}
	}

	/**
	 * Reads provided {@link InputStream} into <code>byte[]</code>
	 * 
	 * @param in
	 * @return contents of the input stream as an array of bytes
	 * @throws IOException
	 */
	public static byte[] drain(InputStream in) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final byte[] buff = new byte[4096];
		while (true) {
			int read = in.read(buff, 0, buff.length);
			if (read <= 0) {
				break;
			}
			out.write(buff, 0, read);
		}
		return out.toByteArray();
	}

	/**
	 * Reads provided {@link InputStream} into <code>byte[]</code> and closes
	 * it. Wraps the {@link IOException} with a {@link RuntimeException}. Error
	 * message is constructed using
	 * {@link MessageFormat#format(String, Object...)}.
	 * 
	 * @param in
	 *            input stream to read
	 * @param errorMessage
	 *            message given to runtime exception in case of error
	 * @param errorParams
	 *            parameters used to construct error message
	 * @return contents of the input stream as an array of bytes
	 */
	public static byte[] drain(InputStream in, String errorMessage, Object... errorParams) {
		try {
			return drain(in);
		} catch (IOException e) {
			throw new RuntimeException(message(errorMessage, errorParams), e);
		} finally {
			close(in, errorMessage, errorParams);
		}
	}

	private static String message(String errorMessage, Object... errorParams) {
		return MessageFormat.format(errorMessage, errorParams);
	}

	private StreamsUtil() {

	}

}
