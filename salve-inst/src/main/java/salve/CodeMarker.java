package salve;

/**
 * Code marker
 * 
 * @author igor.vaynberg
 * 
 */
public class CodeMarker {
	private final String className;
	private final int lineNumber;

	public CodeMarker(String className, int lineNumber) {
		this.className = className;
		this.lineNumber = lineNumber;
	}

	public String getClassName() {
		return className;
	}

	public int getLineNumber() {
		return lineNumber;
	}

}
