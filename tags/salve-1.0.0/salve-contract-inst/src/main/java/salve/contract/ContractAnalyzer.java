/**
 * 
 */
package salve.contract;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.contract.initonce.ClassAnalyzer;
import salve.util.asm.ClassVisitorAdapter;

public class ContractAnalyzer extends ClassAdapter {

	private class MethodAnalyzer extends MethodAdapter {

		public MethodAnalyzer(salve.asmlib.MethodVisitor mv) {
			super(mv);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (desc.startsWith("Lsalve/contract/")) {
				instrument = true;
			}
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			if (opcode == Opcodes.PUTFIELD) {
				if (initOnceAnalyzer.isInitOnce(owner, name)) {
					instrument = true;
				}
			}
			super.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			if (desc.startsWith("Lsalve/contract/")) {
				instrument = true;
			}
			return super.visitParameterAnnotation(parameter, desc, visible);
		}
	}

	private final ClassAnalyzer initOnceAnalyzer;
	private boolean instrument = false;

	public ContractAnalyzer(ClassAnalyzer initOnceAnalyzer) {
		this(new ClassVisitorAdapter(), initOnceAnalyzer);
	}

	public ContractAnalyzer(ClassVisitor cv, ClassAnalyzer initOnceAnalyzer) {
		super(cv);
		this.initOnceAnalyzer = initOnceAnalyzer;
	}

	public boolean shouldInstrument() {
		return instrument;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (!instrument) {
			return new MethodAnalyzer(super.visitMethod(access, name, desc, signature, exceptions));
		} else {
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}
}