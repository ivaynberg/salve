/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package salve.depend.spring.txn;

import org.springframework.transaction.annotation.Transactional;

import salve.InstrumentorMonitor;
import salve.asmlib.AdviceAdapter;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.GeneratorAdapter;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.StaticInitMerger;
import salve.asmlib.Type;


/**
 * INTERNAL
 * <p>
 * Instrumentor for classes and methods annotated with {@link Transactional}
 * </p>
 * 
 * @author ivaynberg
 */
class ClassInstrumentor extends ClassAdapter implements Opcodes, Constants {
	private boolean annotated = false;
	private String owner;
	private final InstrumentorMonitor monitor;
	private int nextAttribute = 0;

	/**
	 * Constructor
	 * 
	 * @param cv
	 *            class visitor
	 * @param monitor
	 *            instrumentor monitor
	 */
	public ClassInstrumentor(ClassVisitor cv, InstrumentorMonitor monitor) {
		super(new StaticInitMerger("_salvespringtxn_", cv));
		this.monitor = monitor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		owner = name;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Method instrumentor
	 * 
	 * @author ivaynberg
	 */
	public class MethodInstrumentor extends AdviceAdapter implements Opcodes,
			Constants {

		private final String methodName;
		private final String methodDesc;
		private final int methodAccess;

		private String attrName;
		private boolean annotated = false;
		private int ptm;
		private int status;

		private Label tryCatchStart = new Label();
		private Label tryCatchEnd = new Label();
		private Label tryCatchHandler = new Label();

		/**
		 * Constructor
		 * 
		 * @param mv
		 *            method visitor
		 * @param access
		 *            method access flags
		 * @param name
		 *            method name
		 * @param desc
		 *            method descriptor
		 */
		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
			methodName = name;
			methodDesc = desc;
			methodAccess = access;
		}

		/**
		 * {@inheritDoc}
		 */
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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visitCode() {
			if (shouldInstrument()) {
				mv.visitTryCatchBlock(tryCatchStart, tryCatchEnd,
						tryCatchHandler, "java/lang/RuntimeException");

				monitor.methodModified(owner, methodAccess, methodName,
						methodDesc);

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

		/**
		 * {@inheritDoc}
		 */
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

				mv.visitLabel(tryCatchStart);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onMethodExit(int opcode) {
			if (shouldInstrument()) {
				if (opcode == ATHROW) {
					/*
					 * we are at a throw statement. if this exception is a
					 * runtime exception we do not need to handle it because it
					 * will be handled and rethrown in our installed try/catch
					 * block
					 */

					// if (e instanceof RuntimeException) { goto skipRollback; }
					mv.visitInsn(Opcodes.DUP);
					mv.visitTypeInsn(INSTANCEOF, RTE_NAME);
					Label skipRollback = new Label();
					mv.visitJumpInsn(IFNE, skipRollback);

					genereateRollbackBytecode();

					mv.visitLabel(skipRollback);
					mv.visitInsn(Opcodes.ATHROW);
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

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			if (shouldInstrument()) {
				mv.visitLabel(tryCatchEnd);
				mv.visitLabel(tryCatchHandler);
				genereateRollbackBytecode();
				mv.visitInsn(Opcodes.ATHROW);
			}
			super.visitMaxs(maxStack, maxLocals);
		}

		private void genereateRollbackBytecode() {
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, ptm);
			mv.visitVarInsn(ALOAD, status);
			mv.visitFieldInsn(GETSTATIC, owner, attrName, TXNATTR_DESC);
			mv.visitMethodInsn(INVOKESTATIC, ADVISERUTIL_NAME,
					ADVISERUTIL_COMPLETE_METHOD_NAME,
					ADVISERUTIL_COMPLETE_METHOD_DESC2);
		}

		/**
		 * @return true if the method should be instrumented (is transactional),
		 *         false otherwise
		 */
		private boolean shouldInstrument() {
			return annotated || ClassInstrumentor.this.annotated;
		}

	}

}
