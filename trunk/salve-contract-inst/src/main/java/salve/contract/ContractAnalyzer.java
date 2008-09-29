/**
 * 
 */
package salve.contract;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.Opcodes;
import salve.contract.initonce.ClassAnalyzer;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.MethodVisitorAdapter;

public class ContractAnalyzer extends ClassVisitorAdapter {

	private class MethodVisitor extends MethodVisitorAdapter {

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (desc.startsWith("Lsalve/contract/")) {
				instrument = true;
			}
			return null;
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
			return null;
		}
	}

	private final ClassAnalyzer initOnceAnalyzer;
	private boolean instrument = false;

	public ContractAnalyzer(ClassAnalyzer initOnceAnalyzer) {
		this.initOnceAnalyzer = initOnceAnalyzer;
	}

	public boolean shouldInstrument() {
		return instrument;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (!instrument) {
			return new MethodVisitor();
		} else {
			return null;
		}
	}
}