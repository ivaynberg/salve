package salve.util.asm;

public class InvalidSignatureException extends RuntimeException {

	public InvalidSignatureException(String signature) {
		super("Signature: " + signature + " is not supported");
	}
}
