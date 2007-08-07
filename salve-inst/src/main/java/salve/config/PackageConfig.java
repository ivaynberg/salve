package salve.config;

import java.util.ArrayList;
import java.util.List;

import salve.Instrumentor;

public class PackageConfig {
	private String packageName;
	private final List<Instrumentor> instrumentors = new ArrayList<Instrumentor>();

	public void add(Instrumentor instrumentor) {
		instrumentors.add(instrumentor);
	}

	public String getPackageName() {
		return packageName;
	}

	public List<Instrumentor> getInstrumentors() {
		return instrumentors;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
