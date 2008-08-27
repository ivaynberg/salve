package salve.contract.pe;

import java.util.Map;

import salve.InstrumentationContext;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.contract.PE;

/**
 * Detects {@link PE} instantiations
 * 
 * @author igor.vaynberg
 * 
 */
public class PeInstantiationValidator extends MethodAdapter {

	/*
	 * NEW salve/contract/PE
	 * 
	 * (DUP)*
	 * 
	 * LDC Lsalve/contract/PEContractInstrumentorTest$TestBean;.class
	 * 
	 * LDC "expression"
	 * 
	 * LDC "mode"
	 * 
	 * 
	 * INVOKESPECIAL salve/contract/PE.<init>(Ljava/lang/Class;Ljava/lang/Class;Ljava
	 * /lang/String;)V
	 */
	private static enum PeState {
		LDC_CLASS, LDC_EXPR, LDC_MODE, INVOKESPECIAL
	}

	private PeState state;
	private String className;
	private String expression;
	private String mode;

	private final InstrumentationContext ctx;

	public PeInstantiationValidator(InstrumentationContext ctx, MethodVisitor mv) {
		super(mv);
		this.ctx = ctx;
	}

	public String getMode() {
		return mode;
	}

	protected void validatePeInstantiation(String className, String expression, String mode) {
		AccessorCollector collector = new AccessorCollector(ctx);
		Policy policy = new TestPolicy();
		String[] parts = expression.split("\\.");
		if (parts.length < 1) {
			throw new IllegalArgumentException("PE Expression: " + expression + " must have at least one part");
		}
		String cn = className;
		Accessor accessor = null;
		for (String part : parts) {
			Map<Accessor.Type, Accessor> accessors = collector.collect(cn, part, mode, accessor);
			if (accessors.isEmpty()) {
				throw new RuntimeException("Could not resolve expression part: " + part + " in class: " + cn);
			}
			accessor = policy.choose(accessors);
			cn = accessor.getReturnTypeName();
		}
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		state = null;
		mv.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		state = null;
		mv.visitIincInsn(var, increment);
	}

	@Override
	public void visitInsn(int opcode) {
		if (state == PeState.LDC_CLASS && opcode == Opcodes.DUP) {
			// dup is optional, do not reset state
			// noop
		} else {
			state = null;
		}
		mv.visitInsn(opcode);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		state = null;
		mv.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		state = null;
		mv.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (state == PeState.LDC_CLASS) {
			className = cst.toString();
			state = PeState.LDC_EXPR;
		} else if (state == PeState.LDC_EXPR) {
			expression = cst.toString();
			state = PeState.LDC_MODE;
		} else if (state == PeState.LDC_MODE) {
			mode = cst.toString();
			state = PeState.INVOKESPECIAL;
		} else {
			state = null;
		}
		mv.visitLdcInsn(cst);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		state = null;
		mv.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (state == PeState.INVOKESPECIAL && opcode == Opcodes.INVOKESPECIAL && "salve/contract/PE".equals(owner)
				&& "<init>".equals(name) && "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V".equals(desc)) {

			if (!className.startsWith("L") || !className.endsWith(";")) {
				throw new IllegalStateException("Invalid class name detected: " + className
						+ ", expected class name in format Lpackage/classname;");
			}

			validatePeInstantiation(className.substring(1, className.length() - 1), expression, mode);
		}
		state = null;
		mv.visitMethodInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitMultiANewArrayInsn(String desc, int dims) {
		state = null;
		mv.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		state = null;
		mv.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitTypeInsn(int opcode, String desc) {
		if (opcode == Opcodes.NEW && desc.equals("salve/contract/PE")) {
			state = PeState.LDC_CLASS;
		} else {
			state = null;
		}
		mv.visitTypeInsn(opcode, desc);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		state = null;
		mv.visitVarInsn(opcode, var);
	}
}
