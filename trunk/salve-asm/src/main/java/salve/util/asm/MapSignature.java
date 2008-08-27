/**
 * 
 */
package salve.util.asm;

import salve.asmlib.signature.SignatureReader;
import salve.asmlib.signature.SignatureVisitor;

/**
 * Parsed representation of a Map signature
 * 
 * @author igor.vaynberg
 * 
 */
public class MapSignature {
	private final class Visitor extends SignatureVisitorAdapter {
		private int mode;
		private final String signature;

		public Visitor(String signature) {
			this.signature = signature;
		}

		@Override
		public void visitClassType(String name) {
			if (mode == 0) {
				// do nothing
			} else if (mode == 1) {
				keyTypeClassName = name;
			} else if (mode == 2) {
				valueTypeClassName = name;
			} else {
				throw new InvalidSignatureException(signature);
			}
		}

		@Override
		public SignatureVisitor visitTypeArgument(char wildcard) {
			mode++;
			return this;
		}

	}

	private String keyTypeClassName;
	private String valueTypeClassName;

	public MapSignature() {
	}

	public String getKeyTypeClassName() {
		return keyTypeClassName;
	}

	public String getValueTypeClassName() {
		return valueTypeClassName;
	}

	public void parse(String signature) throws InvalidSignatureException {
		SignatureReader reader = new SignatureReader(signature);
		reader.accept(new Visitor(signature));
		if (keyTypeClassName == null || valueTypeClassName == null) {
			throw new InvalidSignatureException(signature);
		}
	}

}