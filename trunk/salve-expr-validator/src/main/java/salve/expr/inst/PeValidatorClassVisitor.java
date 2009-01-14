package salve.expr.inst;

import salve.InstrumentationContext;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.util.asm.MethodVisitorAdapter;

public class PeValidatorClassVisitor extends ClassAdapter {
	private final InstrumentationContext ctx;
	private Type owner;
	private final Type pe;
	private final Arg[] constructor;

	public PeValidatorClassVisitor(Type pe, Arg[] constructor, InstrumentationContext ctx, ClassVisitor cv) {
		super(cv);
		this.ctx = ctx;
		this.pe = pe;
		this.constructor = constructor;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		owner = Type.getType("L" + name + ";");
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null) {
			mv = new MethodVisitorAdapter();
		}
		return new PeValidatorMethodVisitor(pe, constructor, owner, ctx, mv);
	}

}
