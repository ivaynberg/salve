package salve.spring.txn;

import java.lang.annotation.Annotation;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;

import salve.dependency.DependencyLibrary;
import salve.dependency.Key;

public class AdviserUtil {
	private AdviserUtil() {

	}

	public static void complete(PlatformTransactionManager mgr,
			TransactionStatus st, TransactionAttribute attr) {
		mgr.commit(st);
	}

	public static void complete(Throwable t, PlatformTransactionManager mgr,
			TransactionStatus st, TransactionAttribute attr) {
		if (attr.rollbackOn(t)) {
			// XXX wrap any rte from rollback() nicely so we can still see
			// original error in param 't'
			mgr.rollback(st);
		} else {
			mgr.commit(st);
		}
	}

	public static PlatformTransactionManager locateTransactionManager() {
		TransactionAttributeSourceAdvisor adviser = (TransactionAttributeSourceAdvisor) DependencyLibrary
				.locate(AdviserKey.INSTANCE);

		// XXX HACK HACK HACK
		TransactionAspectSupport base = (TransactionAspectSupport) adviser
				.getAdvice();

		return base.getTransactionManager();
	}

	private static class AdviserKey implements Key {

		public static final AdviserKey INSTANCE = new AdviserKey();

		private static final long serialVersionUID = 1L;

		private static final Annotation[] EMPTY = new Annotation[] {};

		private AdviserKey() {

		}

		public Annotation[] getAnnotations() {
			return EMPTY;
		}

		public Class<?> getType() {
			return TransactionAttributeSourceAdvisor.class;
		}
	}

}
