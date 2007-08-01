package salve.contract.impl;

import java.util.ArrayList;
import java.util.List;

import salve.org.objectweb.asm.AnnotationVisitor;
import salve.org.objectweb.asm.ClassAdapter;
import salve.org.objectweb.asm.ClassVisitor;
import salve.org.objectweb.asm.Label;
import salve.org.objectweb.asm.MethodVisitor;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.AdviceAdapter;

public class ClassInstrumentor extends ClassAdapter {
	private String owner;

	public ClassInstrumentor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		owner = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		return new MethodInstrumentor(cv.visitMethod(access, name, desc,
				signature, exceptions), access, name, desc);
	}

	private static class Arg {
		private int index;
		private String name;
		private String desc;
		private boolean notNull;
		private boolean notEmpty;

	}

	private class MethodInstrumentor extends AdviceAdapter implements Constants {

		private final String methodName;
		private final String methodDesc;
		private final int methodAccess;

		private boolean notNull = false;
		private boolean notEmpty = false;
		private final List<Arg> args = new ArrayList<Arg>(4);

		private final Label methodStart = new Label();
		private final Label argsCheck = new Label();
		private final Label returnValueCheck = new Label();

		public MethodInstrumentor(MethodVisitor mv, int access, String name,
				String desc) {
			super(mv, access, name, desc);
			methodAccess = access;
			methodName = name;
			methodDesc = desc;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (NOTEMPTY_DESC.equals(desc)) {
				notEmpty = true;
				return null;
			} else if (NOTNULL_DESC.equals(desc)) {
				notNull = true;
				return null;
			}
			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitLocalVariable(String name, String desc,
				String signature, Label start, Label end, int index) {
			Arg arg = getArg(index - 1);
			if (arg != null) {
				arg.name = name;
				arg.desc = desc;
			}
			super.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			{
				mark(argsCheck);
				for (Arg arg : args) {
					final Label end = new Label();
					loadArg(arg.index);
					ifNonNull(end);
					newInstance(ILLEGALARGEX);
					dup();
					String msg = "Argument ";
					if (arg.name != null) {
						msg += "`" + arg.name + "`";
					} else {
						msg += "at index " + arg.index;
						if (arg.desc != null) {
							final String argType = Type.getType(arg.desc)
									.getClassName();
							msg += " and type " + argType;
						}
					}
					msg += " cannot be null";
					push(msg);
					invokeConstructor(ILLEGALARGEX, ILLEGALARGEX_INIT);
					throwException();
					mark(end);
				}
				goTo(methodStart);
			}

			if (notNull) {
				Label end = new Label();
				mark(returnValueCheck);
				dup();
				ifNonNull(end);
				newInstance(ILLEGALSTATEEX);
				dup();
				String msg = "Method `";
				// TODO better method name
				msg += methodName + methodDesc;
				msg += "` cannot return a null value";
				push(msg);
				invokeConstructor(ILLEGALSTATEEX, ILLEGALSTATEEX_INIT);
				throwException();
				mark(end);
				returnValue();
			}

			super.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter,
				String desc, boolean visible) {
			int index = parameter;
			if (NOTEMPTY_DESC.equals(desc)) {
				getOrCreateArg(index).notEmpty = true;
				return null;
			} else if (NOTNULL_DESC.equals(desc)) {
				getOrCreateArg(index).notNull = true;
				return null;
			}
			return super.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		protected void onMethodEnter() {
			goTo(argsCheck);
			mark(methodStart);
		}

		@Override
		protected void onMethodExit(int opcode) {
			if (opcode == ARETURN) {
				goTo(returnValueCheck);
			}
		}

		/**
		 * @param index
		 */
		private Arg getArg(int index) {
			for (Arg arg : args) {
				if (arg.index == index) {
					return arg;
				}
			}
			return null;
		}

		private Arg getOrCreateArg(int index) {
			Arg arg = getArg(index);
			if (arg == null) {

				arg = new Arg();
				arg.index = index;
				args.add(arg);
			}
			return arg;
		}

	}

}
