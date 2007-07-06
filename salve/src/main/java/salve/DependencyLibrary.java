package salve;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DependencyLibrary {
	private static final List<Locator> locators = new CopyOnWriteArrayList<Locator>();

	public static final void addLocator(Locator locator) {
		locators.add(locator);
	}

	public static final void clear() {
		locators.clear();
	}

	public static final Object locate(Key key)
			throws DependencyNotFoundException {
		for (Locator locator : locators) {
			Object dependency = locator.locate(key);
			if (dependency != null) {
				checkType(dependency, key, locator);
				return dependency;
			}
		}
		throw new DependencyNotFoundException(key);
	}

	private static void checkType(Object dependency, Key key, Locator locator) {
		final Class locatedType = dependency.getClass();
		final Class requiredType = key.getDependencyClass();
		if (!requiredType.isAssignableFrom(locatedType)) {
			throw new IllegalStateException(String.format(
					"Locator returned dependency of invalid type. "
							+ "Located type: %s. Required type: %s. "
							+ "Key: %s. Locator: %s", locatedType,
					requiredType, key, locator));
		}
	}
}
