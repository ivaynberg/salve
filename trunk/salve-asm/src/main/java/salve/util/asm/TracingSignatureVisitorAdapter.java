package salve.util.asm;

import salve.asmlib.signature.SignatureVisitor;

/**
 * Adapter for {@link SignatureVisitor}
 * 
 * @author igor.vaynberg
 * 
 */
public class TracingSignatureVisitorAdapter implements SignatureVisitor {
	private static int counter = 0;

	private final int id = counter++;

	/** {@inheritDoc} */
	public SignatureVisitor visitArrayType() {
		System.out.print(id);
		System.out.println("visitArrayType()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public void visitBaseType(char descriptor) {
		System.out.print(id);
		System.out.println("visitBaseType(" + descriptor + ")");
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitClassBound() {
		System.out.print(id);
		System.out.println("visitClassBound()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public void visitClassType(String name) {
		System.out.print(id);
		System.out.println("visitClassType(" + name + ")");
	}

	/** {@inheritDoc} */
	public void visitEnd() {
		System.out.print(id);
		System.out.println("visitEnd");
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitExceptionType() {
		System.out.print(id);
		System.out.println("visitExceptionType()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public void visitFormalTypeParameter(String name) {
		System.out.print(id);
		System.out.println("visitFormTypeParameter(" + name + ")");
	}

	/** {@inheritDoc} */
	public void visitInnerClassType(String name) {
		System.out.print(id);
		System.out.println("visitInnerClassType(" + name + ")");
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitInterface() {
		System.out.print(id);
		System.out.println("visitInterface()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitInterfaceBound() {
		System.out.print(id);
		System.out.println("visitInterfaceBound()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitParameterType() {
		System.out.print(id);
		System.out.println("visitParameterType()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitReturnType() {
		System.out.print(id);
		System.out.println("visitReturnType()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitSuperclass() {
		System.out.print(id);
		System.out.println("visitSuperClass()");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public void visitTypeArgument() {
		System.out.print(id);
		System.out.println("visitTypeArgument()");
	}

	/** {@inheritDoc} */
	public SignatureVisitor visitTypeArgument(char wildcard) {
		System.out.print(id);
		System.out.println("visitTypeArgument(" + wildcard + ")");
		return new TracingSignatureVisitorAdapter();
	}

	/** {@inheritDoc} */
	public void visitTypeVariable(String name) {
		System.out.print(id);
		System.out.println("visitTypeVariable(" + name + ")");
	}

}
