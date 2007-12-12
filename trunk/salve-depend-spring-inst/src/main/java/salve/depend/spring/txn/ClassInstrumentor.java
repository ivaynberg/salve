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
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.StaticInitMerger;
import salve.asmlib.Type;
import salve.util.asm.GeneratorAdapter;

/**
 * INTERNAL
 * <p>
 * Instrumentor for classes and methods annotated with {@link Transactional}
 * </p>
 * 
 * @author ivaynberg
 */
class ClassInstrumentor extends ClassAdapter implements Opcodes, Constants {
	private String owner;
	private final InstrumentorMonitor monitor;
	private int nextAttribute = 0;
	private final ClassAnalyzer analyzer;

	/**
	 * Constructor
	 * 
	 * @param cv
	 *            class visitor
	 * @param monitor
	 *            instrumentor monitor
	 */
	public ClassInstrumentor(ClassAnalyzer analyzer, ClassVisitor cv,
			InstrumentorMonitor monitor) {
		super(new StaticInitMerger("_salvespringtxn_", cv));
		this.monitor = monitor;
		this.analyzer = analyzer;
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
			return cv.visitAnnotation(SPRINGTRANSACTIONAL_DESC, visible);
		} else {
			return cv.visitAnnotation(desc, visible);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	/*private static class Foo {
		private static TransactionAttribute attr;

		private void x(String foo) {
			Object txn = AdviserUtil.begin(attr, "foo");
		}

		private int r(int a) {
			x("bah");
			return 0;
		}

		public void a() {
			Object info = AdviserUtil.begin(null, null);
			try {
				System.out.println("hello");
				b();
			} catch (RuntimeException e) {
				AdviserUtil.finish(e, info);
				throw e;
			} finally {
				AdviserUtil.cleanup(info);
			}
			AdviserUtil.finish(info);
		}

		private void cleanup() {
		}

		private void commit() {
		}

		private void rollback() {
		}

		private void b() {

		}
	}
*/
	// ////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		if (analyzer
				.shouldInstrument(access, name, desc, signature, exceptions)) {

			final String delegateMethodName = "__salve_txn$" + name;

			// generate field to hold transactional attribute class for this
			// method
			final String attrName = generateTransactionalAttributeField(
					delegateMethodName, desc);

			final String txnName = owner + name;

			MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
					exceptions);
			mv.visitCode();

			// //////////////////////////////////////////////////////////////////////////////////////////

			GeneratorAdapter gen = new GeneratorAdapter(mv, access, name, desc);

			Label start = new Label();
			Label end = new Label();
			Label exception = new Label();
			Label cleanupAndCommit = new Label();
			Label cleanupAndThrow = new Label();

			gen.visitTryCatchBlock(start, end, exception,
					"java/lang/Throwable");

			gen.visitTryCatchBlock(start, cleanupAndThrow, cleanupAndThrow,
					null);

			// Object status;
			int txn = gen.newLocal(Type.getType("Ljava/lang/Object;"));

			// status=AdviserUtil.begin(attr, "joinpoint");
			mv.visitFieldInsn(GETSTATIC, owner, attrName, TXNATTR_DESC);
			mv.visitLdcInsn(txnName);
			mv
					.visitMethodInsn(
							INVOKESTATIC,
							"salve/depend/spring/txn/AdviserUtil",
							"begin",
							"(Lsalve/depend/spring/txn/TransactionAttribute;Ljava/lang/String;)Ljava/lang/Object;");
			mv.visitVarInsn(ASTORE, txn);

			mv.visitLabel(start);

			// call delegate
			gen.loadThis();
			gen.loadArgs();
			gen.visitMethodInsn(INVOKESPECIAL, owner, delegateMethodName, desc);

			gen.visitLabel(end);
			gen.visitJumpInsn(GOTO, cleanupAndCommit);

			gen.visitLabel(exception);

			//mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			//mv.visitLdcInsn(">>>EXCEPTION LABEL");
			//mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			
			// dup exception
			gen.dup();

			// finish(ex, status);
			gen.loadLocal(txn);
			gen.visitMethodInsn(INVOKESTATIC,
					"salve/depend/spring/txn/AdviserUtil", "finish",
					"(Ljava/lang/Throwable;Ljava/lang/Object;)V");

			// throw e;
			gen.visitInsn(ATHROW);

			gen.visitLabel(cleanupAndThrow);
			
			gen.loadLocal(txn);
			gen.visitMethodInsn(INVOKESTATIC,
					"salve/depend/spring/txn/AdviserUtil", "cleanup",
					"(Ljava/lang/Object;)V");

			gen.visitInsn(ATHROW);

			gen.visitLabel(cleanupAndCommit);

			gen.loadLocal(txn);
			gen.visitMethodInsn(INVOKESTATIC,
					"salve/depend/spring/txn/AdviserUtil", "cleanup",
					"(Ljava/lang/Object;)V");

			gen.loadLocal(txn);
			gen.visitMethodInsn(INVOKESTATIC,
					"salve/depend/spring/txn/AdviserUtil", "finish",
					"(Ljava/lang/Object;)V");

			gen.returnValue();

			gen.endMethod();

			// //////////////////////////////////////////////////////////////////////////////////////////

			mv = cv.visitMethod(access, delegateMethodName, desc, signature,
					exceptions);

			return new MethodInstrumentor(mv);
		} else {
			return cv.visitMethod(access, name, desc, signature, exceptions);
		}
	}

	/**
	 * Method instrumentor
	 * 
	 * @author ivaynberg
	 */
	public class MethodInstrumentor extends MethodAdapter implements Opcodes,
			Constants {

		public MethodInstrumentor(MethodVisitor mv) {
			super(mv);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			// rewrite @Transactional as @SpringTransactional
			if (TRANSACTIONAL_DESC.equals(desc)) {
				return mv.visitAnnotation(SPRINGTRANSACTIONAL_DESC, visible);
			} else {
				return mv.visitAnnotation(desc, visible);
			}
		}

	}

	private String generateTransactionalAttributeField(String methodName,
			String methodDesc) {
		String attrName = "_salvestxn$attr" + nextAttribute++;
		cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, attrName,
				TXNATTR_DESC, null, null);

		GeneratorAdapter clinit = new GeneratorAdapter(cv.visitMethod(
				ACC_STATIC, "<clinit>", "()V", null, null), ACC_STATIC,
				"<clinit>", "()V");
		clinit.visitCode();
		clinit.visitTypeInsn(NEW, TXNATTR_NAME);
		clinit.visitInsn(DUP);
		clinit.visitLdcInsn(Type.getObjectType(owner));
		clinit.visitLdcInsn(methodName);

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

		return attrName;
	}

}
