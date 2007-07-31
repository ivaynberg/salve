package salve;

public class CannotLoadBytecodeException extends RuntimeException {
	public CannotLoadBytecodeException(String className) {
		super("Cannot load bytecode for class " + className);
	}
}
