package salve.spring.txn;

import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Opcodes;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.AdviceAdapter;
import salve.org.objectweb.asm.commons.StaticInitMerger;
import salve.util.asm.GeneratorAdapter;

public class ClassInstrumentor extends ClassAdapter implements Opcodes,
		Constants {
	private boolean annotated = false;
	private String owner;

	private int nextAttribute = 0;

	public ClassInstrumentor(ClassVisitor cv) {
		super(new StaticInitMerger("_salvesprinttxn_", cv));
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		owner = name;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		// rewrite @Transactional as @SpringTransactional
		if (TRANSACTIONAL_DESC.equals(desc)) {
			annotated = true;
			return cv.visitAnnotation(SPRINGTRANSACTIONAL_DESC, visible);
		} else if (SPRINGTRANSACTIONAL_DESC.equals(desc)) {
			// if we see a @SpringTransactional annot this class has
			// already been instrumented
			annotated = false;
			return cv.visitAnnotation(desc, visible);
		} else {
			return cv.visitAnnotation(desc, visible);
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		if ((access & ACC_STATIC) != 0) {
			return cv.visitMethod(access, name, desc, signature, exceptions);
		}

		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		return new MethodInstrumentor(mv, access, name, desc);
	}

	public class MethodInstrumentor extends AdviceAdapter implements Opcodes,
			Constants {

		private final String methodName;
		private final String methodDesc;

		private String attrName;
		private boolean annotated = false;
		private int ptm;
		private int status;

		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
			methodName = name;
			methodDesc = desc;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			// rewrite @Transactional as @SpringTransactional
			if (TRANSACTIONAL_DESC.equals(desc)) {
				annotated = true;
				return mv.visitAnnotation(SPRINGTRANSACTIONAL_DESC, visible);
			} else if (SPRINGTRANSACTIONAL_DESC.equals(desc)) {
				// if we see a @SpringTransactional annot this method has
				// already been instrumented
				annotated = false;
				return mv.visitAnnotation(desc, visible);
			} else {
				return mv.visitAnnotation(desc, visible);
			}
		}

		@Override
		public void visitCode() {
			if (shouldInstrument()) {
				attrName = "_salvestxn$attr" + nextAttribute++;
				cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, attrName,
						TXNATTR_DESC, null, null);

				GeneratorAdapter clinit = new GeneratorAdapter(cv.visitMethod(
						ACC_STATIC, "<clinit>", "()V", null, null), ACC_STATIC,
						"<clinit>", "()V");
				clinit.visitCode();
				clinit.visitTypeInsn(NEW, TXNATTR_NAME);
				clinit.visitInsn(DUP);
				clinit.visitLdcInsn(Type.getObjectType(owner));
				clinit.visitLdcInsn(this.methodName);

				// create array of method argument types
				Type[] types = Type.getArgumentTypes(methodDesc);
				clinit.push(types.length);
				clinit.visitTypeInsn(ANEWARRAY, "java/lang/Class");

				for (int i = 0; i < types.length; i++) {
					final Type type = types[i];
					clinit.visitInsn(DUP);
					clinit.push(i);

					// clinit.visitFieldInsn(GETSTATIC, "java/lang/Integer",
					// "TYPE", "Ljava/lang/Class;");
					// clinit.visitLdcInsn(type);

					clinit.push(type);
					clinit.visitInsn(AASTORE);
				}
				clinit.visitMethodInsn(INVOKESPECIAL, TXNATTR_NAME, "<init>",
						TXNATTR_INIT_DESC);
				clinit.visitFieldInsn(PUTSTATIC, owner, attrName, TXNATTR_DESC);
				clinit.visitInsn(RETURN);
				clinit.visitMaxs(0, 0);
				clinit.visitEnd();
			}

			super.visitCode();
		}

		@Override
		protected void onMethodEnter() {
			if (shouldInstrument()) {
				ptm = newLocal(Type.getType(PTM_DESC));
				status = newLocal(Type.getType(STATUS_DESC));

				// ptm=AdviserUtil.locateTransactionManager();
				mv.visitMethodInsn(INVOKESTATIC, ADVISERUTIL_NAME,
						ADVISERUTIL_LOCATE_METHOD_NAME,
						ADVISERUTIL_LOCATE_METHOD_DESC);
				mv.visitVarInsn(ASTORE, ptm);

				// TransactionStatus status=ptm.getTransaction(attrname)

				mv.visitVarInsn(ALOAD, ptm);
				mv.visitFieldInsn(GETSTATIC, owner, attrName, TXNATTR_DESC);
				mv.visitMethodInsn(INVOKEINTERFACE, PTM_NAME,
						PTM_GETTXN_METHOD_NAME, PTM_GETTXN_METHOD_DESC);
				mv.visitVarInsn(ASTORE, status);
			}
		}

		@Override
		protected void onMethodExit(int opcode) {
			if (shouldInstrument()) {
				if (opcode == ATHROW) {
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, ptm);
					mv.visitVarInsn(ALOAD, status);
					mv.visitFieldInsn(GETSTATIC, owner, attrName, TXNATTR_DESC);
					mv.visitMethodInsn(INVOKESTATIC, ADVISERUTIL_NAME,
							ADVISERUTIL_COMPLETE_METHOD_NAME,
							ADVISERUTIL_COMPLETE_METHOD_DESC2);
				} else {
					mv.visitVarInsn(ALOAD, ptm);
					mv.visitVarInsn(ALOAD, status);
					mv.visitFieldInsn(GETSTATIC, owner, attrName, TXNATTR_DESC);
					mv.visitMethodInsn(INVOKESTATIC, ADVISERUTIL_NAME,
							ADVISERUTIL_COMPLETE_METHOD_NAME,
							ADVISERUTIL_COMPLETE_METHOD_DESC);

				}
			}
		}

		private boolean shouldInstrument() {
			return annotated || ClassInstrumentor.this.annotated;
		}

	}

}
