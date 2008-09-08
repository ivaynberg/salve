package salve.contract.pe;

import salve.CodeMarker;
import salve.asmlib.Type;

/**
 * Property expression data being tracked by bytecode reader
 * 
 * @author ivaynberg
 * 
 */
public class PeDefinition {
	private Type type;
	private String expression;
	private String mode;
	private CodeMarker marker;

	public void clear() {
		type = null;
		expression = null;
		mode = "rw";
		marker = null;
	}

	public String getExpression() {
		return expression;
	}

	public CodeMarker getMarker() {
		return marker;
	}

	public String getMode() {
		return mode;
	}

	public Type getType() {
		return type;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setMarker(CodeMarker marker) {
		this.marker = marker;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
