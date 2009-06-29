package salve.util.asm;

import salve.asmlib.signature.SignatureVisitor;

/**
 * Adapter for {@link SignatureVisitor}
 * 
 * @author igor.vaynberg
 * 
 */
public class SignatureVisitorAdapter implements SignatureVisitor {

	/** {@inheritDoc} */
	public SignatureVisitor visitArrayType() {
		return this;
	}

	/** {@inheritDoc} */
	public void visitBaseType(char descriptor) {
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitClassBound() {
		return this;
	}

	/** {@inheritDoc} */
	public void visitClassType(String name) {
	}

	/** {@inheritDoc} */
	public void visitEnd() {

	}

	/** {@inheritDoc} */
	public SignatureVisitor visitExceptionType() {
		return this;
	}

	/** {@inheritDoc} */
	public void visitFormalTypeParameter(String name) {
	}

	/** {@inheritDoc} */
	public void visitInnerClassType(String name) {
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitInterface() {
		return this;
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitInterfaceBound() {
		return this;
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitParameterType() {
		// System.out.println("visit param type");
		return this;
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitReturnType() {
		return this;
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitSuperclass() {
		return this;
	}

	/** {@inheritDoc} */
	public void visitTypeArgument() {
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitTypeArgument(char wildcard) {
		return this;
	}

	/** {@inheritDoc} */
	public void visitTypeVariable(String name) {
	}

}
