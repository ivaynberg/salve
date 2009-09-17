package salve.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import salve.Bytecode;
import salve.BytecodeLoader;
import salve.asmlib.ClassReader;
import salve.util.ListMap;
import salve.util.asm.ClassVisitorAdapter;

public class ProjectModel {
	private BytecodeLoader loader;

	private final Map<String, ClassModel> classes = new HashMap<String, ClassModel>();

	private final ListMap<String, UpdateListener> listeners = new ListMap<String, UpdateListener>();

	public ProjectModel() {
	}

	void add(byte[] bytecode) {
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}
		ClassReader reader = new ClassReader(bytecode);
		reader.accept(new ModelUpdateVisitor(this, new ClassVisitorAdapter()), 0);
	}

	ProjectModel add(ClassModel cm) {
		classes.put(cm.getName(), cm);
		return this;
	}

	public void clear() {
		listeners.clear();
		classes.clear();
	}

	public ClassModel getClass(String name) {
		ClassModel model = classes.get(name);
		if (model == null) {
			Bytecode bytecode = loader.loadBytecode(name);
			if (bytecode != null) {
				if (bytecode.getBytes() == null) {
					// FIXME debug
					int a = 0;
					int b = a + 1;// debug
					bytecode = loader.loadBytecode(name);
				}
				add(bytecode.getBytes());
				model = classes.get(name);
			}
		}
		return model;
	}

	BytecodeLoader getLoader() {
		return loader;
	}

	public void notifyUpdateListeners(String cn) {
		Iterator<UpdateListener> it = listeners.get(cn).iterator();
		while (it.hasNext()) {
			switch (it.next().updated()) {
				case REMOVE:
					it.remove();
			}
		}

	}

	public void register(String cn, UpdateListener listener) {
		listeners.add(cn, listener);
	}

	public ProjectModel setLoader(BytecodeLoader loader) {
		this.loader = loader;
		return this;
	}

	public void update(byte[] bytecode) {
		ClassReader reader = new ClassReader(bytecode);

		notifyUpdateListeners(reader.getClassName());
		classes.remove(reader.getClassName());

		reader.accept(new ModelUpdateVisitor(this, new ClassVisitorAdapter()), 0);

	}
}
