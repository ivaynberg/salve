package salve.contract.pe;

import salve.InstrumentationContext;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;

public class PeValidatorMethodVisitor extends MethodAdapter {
	/*
	 * NEW salve/contract/PE
	 * 
	 * (DUP)
	 * 
	 * LDC Lsalve/contract/PEContractInstrumentorTest$TestBean;.class
	 * 
	 * LDC "expression"
	 * 
	 * LDC "mode"
	 * 
	 * 
	 * INVOKESPECIAL
	 * salve/contract/PE.<init>(Ljava/lang/Class;Ljava/lang/Class;Ljava
	 * /lang/String;)V
	 */
	private static enum PeState {
		LDC_CLASS, LDC_EXPR, LDC_MODE, INVOKESPECIAL
	}

	private PeState state;
	private String classDesc;
	private String expression;
	private String mode;

	private final InstrumentationContext ctx;

	private final PeValidator validator;

	public PeValidatorMethodVisitor(InstrumentationContext ctx, MethodVisitor mv) {
		super(mv);
		this.ctx = ctx;
		validator = new PeValidator(ctx);
	}

	public String getMode() {
		return mode;
	}

	protected void validatePeInstantiation(String className, String expression, String mode) {
		validator.validate(className, expression, mode);
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
			classDesc = cst.toString();
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
		if (opcode == Opcodes.INVOKESPECIAL) {
			if ("salve/contract/PE".equals(owner) && "<init>".equals(name)) {
				if (state == PeState.LDC_MODE) {
					// class/expr constructor used, default the mode to rw
					mode = "rw";
					state = PeState.INVOKESPECIAL;
				}
				if (state == PeState.INVOKESPECIAL && "salve/contract/PE".equals(owner)) {
					validatePeInstantiation(Type.getType(classDesc).getInternalName(), expression, mode);
				}
			}
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
