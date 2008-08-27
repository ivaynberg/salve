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
		System.out.println("visit array type");
		return this;
	}

	/** {@inheritDoc} */
	public void visitBaseType(char descriptor) {
		System.out.println("visit base type: " + descriptor);
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitClassBound() {
		System.out.println("visit class bound");
		return this;
	}

	/** {@inheritDoc} */
	public void visitClassType(String name) {
		System.out.println("visit class type: " + name);
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
		System.out.println("visit formal type param: " + name);
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
		System.out.println("visit param type");
		return this;
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitReturnType() {
		System.out.println("visit return type");
		return this;
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitSuperclass() {
		System.out.println("visit superclass");
		return this;
	}

	/** {@inheritDoc} */
	public void visitTypeArgument() {
		System.out.println("visit type arg");
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitTypeArgument(char wildcard) {
		System.out.println("visit type arg wild: " + wildcard);
		return this;
	}

	/** {@inheritDoc} */
	public void visitTypeVariable(String name) {
		System.out.println("visit type var: " + name);
	}

}
