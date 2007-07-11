package salve.spring.txn;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

import org.springframework.transaction.annotation.Transactional;

import salve.Instrumentor;

public class TransactionalInstrumentor implements Instrumentor {

	public byte[] instrument(ClassLoader loader, String name, byte[] bytecode)
			throws Exception {

		ClassPool pool = new ClassPool(ClassPool.getDefault());
		pool.appendClassPath(new ByteArrayClassPath(name, bytecode));
		pool.appendClassPath(new LoaderClassPath(loader));

		boolean all = false;
		boolean addedSupport = false;

		CtClass clazz = pool.get(name);
		Object[] annots = clazz.getAvailableAnnotations();
		for (Object annot : annots) {
			if (annot instanceof Transactional) {
				all = true;
			}
		}

		for (CtMethod method : clazz.getDeclaredMethods()) {
			if (addedSupport == false) {

			}
		}

		return null;
	}

	private void addSupport(CtClass clazz) throws Exception {
		ClassPool pool = clazz.getClassPool();
		CtClass adapterType = pool.get(TransactionAspectSupportAdapter.class
				.getName());

		CtField adapterField = new CtField(adapterType, "__$txnadapter", clazz);
		adapterField.setModifiers(Modifier.PRIVATE | Modifier.TRANSIENT);
		clazz.addField(adapterField);

		CtMethod method = new CtMethod(CtClass.voidType, "__get$txnadapter",
				new CtClass[] {}, clazz);

		clazz.addMethod(method);

	}
}
