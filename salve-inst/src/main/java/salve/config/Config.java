package salve.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
	List<PackageConfig> packageConfigs = new ArrayList<PackageConfig>();

	public Config() {
		super();
	}

	public void add(PackageConfig config) {
		packageConfigs.add(config);
	}

	public PackageConfig getPackageConfig(String name) {
		for (PackageConfig config : packageConfigs) {
			if (name.startsWith(config.getPackageName() + ".")
					|| name.equals(config.getPackageName())) {
				return config;
			}
		}
		return null;
	}

	public List<PackageConfig> getPackageConfigs() {
		return packageConfigs;
	}

}
