package salve.spring.txn;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.bytecode.annotation.Annotation;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import salve.Instrumentor;
import salve.dependency.impl.DependencyConstants;
import salve.util.JavassistUtil;

public class OldTransactionalInstrumentor implements Instrumentor {

	public static final String __CLASSANNOT = "__txn$classannot";
	private static final String __GETTXNMGR = "__get$txnmgr";
	private static final String __TXNMGR = "__txn$mgr";

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {

		ClassPool pool = new ClassPool();
		pool.appendClassPath(new ByteArrayClassPath(name, bytecode));
		pool.appendClassPath(new LoaderClassPath(loader));
		pool.appendSystemPath();

		boolean all = false;
		boolean supportAdded = false;

		CtClass clazz = pool.get(name);

		Annotation txn = JavassistUtil.getAnnot(clazz, Transactional.class);
		if (txn != null) {
			all = true;
			JavassistUtil.substituteAnnot(clazz, Transactional.class,
					SpringTransactional.class);
			addSupport(clazz);
			supportAdded = true;

		}

		CtMethod[] methods = clazz.getDeclaredMethods();

		for (CtMethod method : methods) {
			boolean needInstrumentation = all;
			if (needInstrumentation == false) {
				for (Object annot : method.getAvailableAnnotations()) {
					if (annot instanceof Transactional) {
						needInstrumentation = true;
					}
				}
			}

			if (needInstrumentation) {
				if (!supportAdded) {
					addSupport(clazz);
				}
				instrument(method);
			}
		}

		return clazz.toBytecode();
	}

	private void addSupport(CtClass clazz) throws Exception {
		System.out.println("^^^^^^^^^ " + clazz.getName());
		ClassPool pool = clazz.getClassPool();

		CtClass transactionManager = pool.get(PlatformTransactionManager.class
				.getName());

		CtField mgr = new CtField(transactionManager, __TXNMGR, clazz);
		mgr.setModifiers(Modifier.PRIVATE | Modifier.TRANSIENT);
		clazz.addField(mgr);

		CtMethod getmgr = new CtMethod(transactionManager, __GETTXNMGR,
				new CtClass[] {}, clazz);
		StringBuilder code = new StringBuilder();
		code.append("{ ");

		code.append("if (").append(__TXNMGR).append("==null) {");
		code.append(__TXNMGR).append("=");
		code.append(AdviserUtil.class.getName());
		code.append(".locateTransactionManager(); }");

		code.append("return ").append(__TXNMGR).append(";}");
		getmgr.setBody(code.toString());
		clazz.addMethod(getmgr);
	}

	private void instrument(CtMethod method) throws Exception {
		if (method.getName().startsWith("__ta$")
				|| method.getName().equals(__GETTXNMGR)) {
			return;
		}

		final CtClass clazz = method.getDeclaringClass();

		String attrtype = TransactionAttribute.class.getName();
		/*
		 * TODO optimize if the class has annot but method doesnt its a waste to
		 * create this field for every method
		 */
		String attrname = "__ta$" + method.getName();
		String params = "Class[0]";
		if (method.getParameterTypes().length > 0) {
			params = "Class[] {"
					+ JavassistUtil.Strings.join(method.getParameterTypes())
					+ "}";

		}

		CtField attr = CtField.make("public static " + attrtype + " "
				+ attrname + " = new " + attrtype + "(" + clazz.getName()
				+ ".class, \"" + method.getName() + "\", new " + params + ");",
				clazz);
		clazz.addField(attr);

		JavassistUtil.substituteAnnot(method, Transactional.class,
				SpringTransactional.class);

		final String innerName = delegateName(method.getName());
		CtMethod inner = new CtMethod(method, clazz, null);

		inner.setName(innerName);
		clazz.addMethod(inner);

		StringBuilder code = new StringBuilder();
		code.append("{");

		code.append(PlatformTransactionManager.class.getName() + " ptm=");
		code.append(__GETTXNMGR).append("();");

		code.append(TransactionStatus.class.getName());
		code.append(" txn=ptm.getTransaction(" + attrname + ");");

		code.append("try {\n\t");
		code.append("java.lang.Object ret=null;");
		// invoke inner
		if (method.getReturnType() != CtClass.voidType) {
			code.append("ret=");
		}
		code.append(innerName).append("(");
		DependencyConstants.insertParamsList(method, code);
		code.append(");");
		// end invoke
		code.append(AdviserUtil.class.getName());
		code.append(".complete(ptm, txn, ");
		code.append(attrname).append(");");
		if (method.getReturnType() != CtClass.voidType) {
			code.append("return (").append(method.getReturnType().getName())
					.append(") ret;");
		}

		for (CtClass ex : method.getExceptionTypes()) {
			code.append("} catch (");
			code.append(ex.getName());
			code.append(" e) {\n\t");
			code.append(AdviserUtil.class.getName());
			code.append(".complete(e, ptm, txn, ");
			code.append(attrname).append(");");
			code.append("throw e;");
		}

		code.append("}");

		code.append("}");// end method
		method.setBody(code.toString());

	}

	public static String delegateName(String methodName) {
		return "__txn$" + methodName;
	}
}
