package salve.config.xml;

import java.util.ArrayList;
import java.util.List;

import salve.Instrumentor;
import salve.Scope;

public class XmlPackage implements Scope {
	private String name;
	private String scopeName;
	private List<Instrumentor> instrumentors = new ArrayList<Instrumentor>();

	public List<Instrumentor> getInstrumentors() {
		return instrumentors;
	}

	public String getName() {
		return name;
	}

	public boolean includes(String className) {
		return className.startsWith(scopeName);
	}

	public void setInstrumentors(List<Instrumentor> instrumentors) {
		this.instrumentors = instrumentors;
	}

	public void setName(String name) {
		this.name = name;
		if (name != null) {
			this.scopeName = name.replace(".", "/");
		}
	}

}
