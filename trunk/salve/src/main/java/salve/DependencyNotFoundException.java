package salve;

/**
 * Exception thrown when dependency cannot be located
 * 
 * @author ivaynberg
 */
public class DependencyNotFoundException extends RuntimeException {
	public DependencyNotFoundException(Key key) {
		// TODO better error message pointing to class.field
		super("Could not find dependency for key: " + key.toString());
	}
}
