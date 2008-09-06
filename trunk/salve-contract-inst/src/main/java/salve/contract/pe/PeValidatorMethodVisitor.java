package salve.contract.pe;

import salve.InstrumentationContext;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;
import salve.contract.impl.Constants;

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
	private final PeDefinition def = new PeDefinition();
	private final InstrumentationContext ctx;

	private final PeValidator validator;

	public PeValidatorMethodVisitor(InstrumentationContext ctx, MethodVisitor mv) {
		super(mv);
		this.ctx = ctx;
		validator = new PeValidator(ctx);
	}

	private void clear() {
		state = null;
		def.clear();
	}

	protected void validatePeInstantiation(PeDefinition data) {
		validator.validate(data);
		clear();
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
			def.setType(Type.getType(cst.toString()));
			state = PeState.LDC_EXPR;
		} else if (state == PeState.LDC_EXPR) {
			def.setExpression(cst.toString());
			state = PeState.LDC_MODE;
		} else if (state == PeState.LDC_MODE) {
			def.setMode(cst.toString());
			state = PeState.INVOKESPECIAL;
		} else {
			state = null;
			def.clear();
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
			if (Constants.PE.getInternalName().equals(owner) && "<init>".equals(name)) {
				if (state == PeState.LDC_MODE) {
					// class/expr constructor used, default the mode to rw
					def.setMode("rw");
					state = PeState.INVOKESPECIAL;
				}
				if (state == PeState.INVOKESPECIAL && Constants.PE.getInternalName().equals(owner)) {
					validatePeInstantiation(def);
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
		clear();
		mv.visitVarInsn(opcode, var);
	}

}
