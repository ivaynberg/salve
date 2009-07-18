package salve.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import salve.BytecodeLoader;
import salve.asmlib.ClassReader;
import salve.util.ListMap;
import salve.util.asm.ClassVisitorAdapter;

public class ProjectModel {
	private final BytecodeLoader loader;

	private final Map<String, ClassModel> classes = new HashMap<String, ClassModel>();

	private final ListMap<String, UpdateListener> listeners = new ListMap<String, UpdateListener>();

	public ProjectModel(BytecodeLoader loader) {
		this.loader = loader;
	}

	ProjectModel add(ClassModel cm) {
		classes.put(cm.getName(), cm);
		return this;
	}

	public ClassModel getClass(String name) {
		ClassModel model = classes.get(name);
		if (model == null) {
			byte[] bytecode = loader.loadBytecode(name);
			if (bytecode != null) {
				update(bytecode);
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
		classes.remove(cn);
	}

	public void register(String cn, UpdateListener listener) {
		listeners.add(cn, listener);
	}

	public void update(byte[] bytecode) {
		ClassReader reader = new ClassReader(bytecode);
		reader.accept(new ModelUpdateVisitor(this, new ClassVisitorAdapter()), 0);
	}
}
