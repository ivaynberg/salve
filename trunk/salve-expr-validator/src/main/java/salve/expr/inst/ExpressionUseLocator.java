package salve.expr.inst;


import salve.CodeMarker;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;

public abstract class ExpressionUseLocator extends MethodAdapter
{

    protected final Type type;
    protected final Arg[] constructor;
    private int state = -1;
    protected final Type owner;
    private final PeDefinition def = new PeDefinition();
    private int lastVisitedLineNumber = 0;

    public ExpressionUseLocator(Type pe, Arg[] constructor, Type owner,  MethodVisitor mv)
    {
        super(mv);
        this.type = pe;
        this.constructor = constructor;
        this.owner = owner;
    }

    protected abstract void validatePeInstantiation(PeDefinition data);

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc)
    {
    	state = -1;
    	mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(int var, int increment)
    {
    	state = -1;
    	mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitInsn(int opcode)
    {
    	if (state == 0 && opcode == Opcodes.DUP) {
    		// dup is optional after NEW, do not reset state
    		// noop
    	} else {
    
    		state = -1;
    	}
    	mv.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand)
    {
    	state = -1;
    	mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label)
    {
    	state = -1;
    	mv.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst)
    {
    	if (state == 0) {
    		def.clear();
    	}
    	if (state >= 0) {
    		switch (constructor[state]) {
    			case EXPRESSION:
    				def.setExpression((String) cst);
    				break;
    			case MODE:
    				def.setMode((String) cst);
    				break;
    			case TYPE:
    				def.setType((Type) cst);
    				break;
    			case OTHER:
    				break;
    		}
    		state++;
    	} else {
    		state = -1;
    	}
    	mv.visitLdcInsn(cst);
    }

    @Override
    public void visitLineNumber(int line, Label start)
    {
    	lastVisitedLineNumber = line;
    	mv.visitLineNumber(line, start);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
    {
    	state = -1;
    	mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc)
    {
    
    	if (opcode == Opcodes.INVOKESPECIAL) {
    		if (type.getInternalName().equals(owner) && "<init>".equals(name)) {
    
    			if (state == constructor.length - 1) {
    				// class/expr constructor used, default the mode to rw
    				def.setMode("rw");
    				state++;
    			}
    			if (state == constructor.length && type.getInternalName().equals(owner)) {
    				if (lastVisitedLineNumber > 0) {
    					CodeMarker marker = new CodeMarker(this.owner.getInternalName(), lastVisitedLineNumber);
    					def.setMarker(marker);
    					lastVisitedLineNumber = 0;
    				}
    				validatePeInstantiation(def);
    				state = -1;
    			} else {
    				CodeMarker marker = new CodeMarker(this.owner.getInternalName(), lastVisitedLineNumber);
    				throw new InstrumentationException("Invalid instantiation of " + owner, marker);
    			}
    		}
    	}
    
    	state = -1;
    	mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims)
    {
    	state = -1;
    	mv.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels)
    {
    	state = -1;
    	mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitTypeInsn(int opcode, String desc)
    {
    	if (opcode == Opcodes.NEW && desc.equals(type.getInternalName())) {
    		state = 0;
    	} else {
    		state = -1;
    	}
    	mv.visitTypeInsn(opcode, desc);
    }

    @Override
    public void visitVarInsn(int opcode, int var)
    {
    	if (state == 0) {
    		def.clear();
    	}
    	if (state >= 0) {
    		switch (constructor[state]) {
    			case OTHER:
    				break;
    			case EXPRESSION:
    			case MODE:
    			case TYPE:
    				CodeMarker marker = new CodeMarker(this.owner.getInternalName(), lastVisitedLineNumber);
    				throw new InstrumentationException("Constructor argument of class: " + owner.getClassName()
    						+ " cannot be a variable", marker);
    		}
    		state++;
    	} else {
    		state = -1;
    	}
    	mv.visitVarInsn(opcode, var);
    }

}