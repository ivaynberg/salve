package salve.contract.pe;

import java.util.Iterator;

import salve.BytecodeLoader;

public class ClassHieararchy implements Iterable<EnhancedClassReader> {

	private final BytecodeLoader loader;
	private final String className;

	public ClassHieararchy(BytecodeLoader loader, String className) {
		super();
		this.loader = loader;
		this.className = className;
	}

	public Iterator<EnhancedClassReader> iterator() {
		return new ClassHieararchyIterator(loader, className);
	}

}
