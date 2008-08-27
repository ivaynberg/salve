/**
 * 
 */
package salve.contract;

import salve.asmlib.AnnotationVisitor;
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
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			if (desc.startsWith("Lsalve/contract/")) {
				instrument = true;
			}
			return null;
		}

	}

	private boolean instrument = false;

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