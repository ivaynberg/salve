package salve.bytecode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import salve.DependencyLibrary;
import salve.Key;

public class ProxyBuilder {
	private static final String PROXY_CLASS_SUFFIX = "$InjectorProxy";

	private static final String DELEGATE_FIELD = "__$delegate";

	private static final String KEY_FIELD = "__$key";

	private static final String[] PARAMS_CACHE = { "", "$1", "$1,$2",
			"$1,$2,$3", "$1,$2,$3,$4", "$1,$2,$3,$4,$5", "$1,$2,$3,$4,$5,$6",
			"$1,$2,$3,$4,$5,$6,$7", "$1,$2,$3,$4,$5,$6,$7,$8",
			"$1,$2,$3,$4,$5,$6,$7,$8,$9", "$1,$2,$3,$4,$5,$6,$7,$8,$9,$10" };

	private final ClassPool pool;
	private final CtClass target;

	private CtClass proxy;

	public ProxyBuilder(Class target) throws NotFoundException {
		super();
		pool = ClassPool.getDefault();
		pool.appendClassPath(new ClassClassPath(target));
		this.target = pool.get(target.getName());
	}

	public ProxyBuilder(final ClassPool pool, Class target)
			throws NotFoundException {
		super();
		this.pool = pool;
		this.target = pool.get(target.getName());
	}

	public ProxyBuilder(final ClassPool pool, CtClass target) {
		super();
		this.pool = pool;
		this.target = target;
	}

	public CtClass build() throws CannotCompileException, NotFoundException {
		// TODO check if the class is not final
		// TODO warn if the proxied class has final methods

		proxy = pool.makeClass(target.getName() + PROXY_CLASS_SUFFIX);
		if (target.isInterface()) {
			proxy.addInterface(target);
		} else {
			proxy.setSuperclass(target);
		}

		addFields();
		addConstructor();
		addDelegatorMethods();
		addSerializationSupport();
		return proxy;
	}

	private boolean acceptMethod(CtMethod method) {
		final int modifiers = method.getModifiers();
		if (Modifier.isStatic(modifiers)) {
			return false;
		}
		if (!Modifier.isPublic(modifiers)) {
			return false;
		}
		if (Object.class.getName().equals(method.getDeclaringClass().getName())) {
			if ("hashCode".equals(method.getName())) {
				return true;
			}
			if ("equals".equals(method.getName())) {
				return true;
			}
			if ("toString".equals(method.getName())) {
				return true;
			}
			return false;
		}
		return true;
	}

	private void addConstructor() throws CannotCompileException,
			NotFoundException {
		CtConstructor init = new CtConstructor(new CtClass[] { pool
				.get(Key.class.getName()) }, proxy);
		StringBuilder code = new StringBuilder();
		code.append("{");
		code.append(KEY_FIELD).append("=$1;");
		code.append(DELEGATE_FIELD).append("=(").append(target.getName())
				.append(")salve.DependencyLibrary.locate(").append(KEY_FIELD)
				.append(");");
		code.append("}");
		init.setBody(code.toString());
		proxy.addConstructor(init);
	}

	private void addDelegatorMethod(CtMethod method)
			throws CannotCompileException, NotFoundException {

		CtMethod delegator = new CtMethod(method, proxy, null);

		// remove native modifier we might have inherited from `method`
		int modifiers = delegator.getModifiers();
		modifiers = Modifier.clear(modifiers, Modifier.NATIVE);
		delegator.setModifiers(modifiers);

		// generate method body
		StringBuilder code = new StringBuilder();

		if (method.getReturnType() != CtClass.voidType) {
			code.append("return ");
		}

		code.append(DELEGATE_FIELD).append(".").append(delegator.getName())
				.append("(");
		codegenParamsList(delegator, code);
		code.append(");");

		delegator.setBody(code.toString());
		proxy.addMethod(delegator);
	}

	private void addDelegatorMethods() throws CannotCompileException,
			NotFoundException {
		for (CtMethod method : target.getMethods()) {
			if (acceptMethod(method)) {
				addDelegatorMethod(method);
			}
		}
	}

	private void addFields() throws CannotCompileException, NotFoundException {
		CtField delegate = new CtField(target, DELEGATE_FIELD, proxy);
		delegate.setModifiers(Modifier.PRIVATE | Modifier.FINAL
				| Modifier.TRANSIENT);

		proxy.addField(delegate);

		CtField key = new CtField(pool.get(Key.class.getName()), KEY_FIELD,
				proxy);
		key.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
		proxy.addField(key);
	}

	private void addSerializationSupport() throws NotFoundException,
			CannotCompileException {
		proxy.addInterface(pool.get(Serializable.class.getName()));

		StringBuilder body = new StringBuilder();
		body.append("{").append("$1.defaultReadObject();").append(
				DELEGATE_FIELD).append("=(").append(target.getName()).append(
				")").append(DependencyLibrary.class.getName()).append(
				".locate(").append(KEY_FIELD).append(");}");
		CtMethod method = CtNewMethod.make(Modifier.PRIVATE, CtClass.voidType,
				"readObject", new CtClass[] { pool.get(ObjectInputStream.class
						.getName()) }, new CtClass[] {
						pool.get(IOException.class.getName()),
						pool.get(ClassNotFoundException.class.getName()) },
				body.toString(), proxy);
		proxy.addMethod(method);

		// private void readObject(java.io.ObjectInputStream in) throws
		// IOException, ClassNotFoundException;

	}

	private void codegenParamsList(CtMethod method, StringBuilder code)
			throws NotFoundException {
		final int params = method.getParameterTypes().length;
		if (params < PARAMS_CACHE.length) {
			code.append(PARAMS_CACHE[params]);
		} else {
			code.append(PARAMS_CACHE[PARAMS_CACHE.length - 1]);
			for (int i = PARAMS_CACHE.length; i < params; i++) {
				code.append(",$").append(i + 1);
			}
		}
	}
}
