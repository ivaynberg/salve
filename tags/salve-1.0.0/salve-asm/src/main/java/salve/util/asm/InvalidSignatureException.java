package salve.util.asm;

public class InvalidSignatureException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidSignatureException(String signature) {
		super("Signature: " + signature + " is not supported");
	}
}
