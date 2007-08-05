package salve.depend;

public class IllegalFieldWriteException extends RuntimeException {
	public IllegalFieldWriteException(String clazz, String field) {
		super("Attempted to write to field `" + field
				+ "` that has been removed from class `" + clazz
				+ "` by salve's dependency instrumentor");
	}
}
