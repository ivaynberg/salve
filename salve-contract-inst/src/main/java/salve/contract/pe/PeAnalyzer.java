package salve.contract.pe;

import salve.InstrumentationContext;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodVisitor;
import salve.util.asm.MethodVisitorAdapter;

public class PeAnalyzer extends ClassAdapter {
	private final InstrumentationContext ctx;

	public PeAnalyzer(InstrumentationContext ctx, ClassVisitor cv) {
		super(cv);
		this.ctx = ctx;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null) {
			mv = new MethodVisitorAdapter();
		}
		return new PeInstantiationValidator(ctx, mv);
	}

}
