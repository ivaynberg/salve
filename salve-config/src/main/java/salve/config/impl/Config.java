package salve.config.impl;

import java.util.ArrayList;
import java.util.List;

public class Config {
	private List<Package> packages = new ArrayList<Package>();

	public List<Package> getPackages() {
		return packages;
	}

	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}
}
