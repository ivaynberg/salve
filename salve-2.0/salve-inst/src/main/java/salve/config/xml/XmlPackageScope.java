package salve.config.xml;

import java.util.HashSet;
import java.util.Set;

import salve.Scope;

/**
 * Implementation of {@link Scope} based on {@link XmlConfig}
 * 
 * @author igor.vaynberg
 * 
 */
public class XmlPackageScope implements Scope {

	private final Set<String> packages = new HashSet<String>();

	void addPackage(String packageName) {
		packages.add(packageName.replace(".", "/"));
	}

	public boolean includes(String className) {
		for (String packageName : packages) {
			if (className.startsWith(packageName)) {
				return true;
			}
		}
		return false;
	}

}
