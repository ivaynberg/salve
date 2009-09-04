package salve.config.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import salve.Instrumentor;
import salve.Scope;

public class XmlConfig implements salve.Config {
	private List<XmlPackage> packages = new ArrayList<XmlPackage>();

	private final Map<Instrumentor, XmlPackageScope> scopes = new ConcurrentHashMap<Instrumentor, XmlPackageScope>();

	public Collection<Instrumentor> getInstrumentors(String className) {
		Collection<Instrumentor> instrumentors = null;

		for (XmlPackage pkg : packages) {
			if (pkg.includes(className)) {
				if (instrumentors == null) {
					instrumentors = new ArrayList<Instrumentor>(pkg.getInstrumentors());
				} else {
					instrumentors.addAll(pkg.getInstrumentors());
				}
			}
		}

		if (instrumentors == null) {
			return Collections.emptyList();
		} else {
			return instrumentors;
		}
	}

	public List<XmlPackage> getPackages() {
		return packages;
	}

	public Scope getScope(Instrumentor instrumentor) {
		Scope scope = scopes.get(instrumentor);
		if (scope == null) {
			scope = Scope.NONE;
		}
		return scope;

	}

	/**
	 * Initializes the config. This method should be called before
	 * {@link XmlConfig} object is about to be used to resolve instrumentors and
	 * after the last {@link #add(XmlPackageConfig)} call.
	 */
	void initialize(ClassLoader instrumentorClassLoader) {
		for (XmlPackage pkg : packages) {

			for (Instrumentor instrumentor : pkg.getInstrumentors()) {

				// update instrumentor scope
				XmlPackageScope scope = scopes.get(instrumentor);
				if (scope == null) {
					scope = new XmlPackageScope();
					scopes.put(instrumentor, scope);
				}

				scope.addPackage(pkg.getName());
			}

		}

	}

	// private Instrumentor instantiateInstrumentor(String instClassName,
	// ClassLoader instrumentorClassLoader) {
	// Class<?> instClass = null;
	// try {
	// instClass = instrumentorClassLoader.loadClass(instClassName);
	// } catch (ClassNotFoundException e) {
	// throw new RuntimeException("Could not load instrumentor class " +
	// instClassName
	// +
	// ", make sure it is available on the classpath at the time of instrumentation");
	// }
	//
	// Object inst;
	// try {
	// inst = instClass.newInstance();
	// } catch (InstantiationException e) {
	// throw new RuntimeException("Could not instantiate instrumentor of class "
	// + instClassName, e);
	// } catch (IllegalAccessException e) {
	// throw new RuntimeException("Could not access instrumentor of class " +
	// instClassName, e);
	// }
	//
	// if (!(inst instanceof Instrumentor)) {
	// throw new
	// RuntimeException(String.format("Instrumentor class %s does not implement %s",
	// instClassName,
	// Instrumentor.class.getName()));
	// }
	//
	// return (Instrumentor) inst;
	// }

	public void setPackages(List<XmlPackage> packages) {
		this.packages = packages;
	}
}
