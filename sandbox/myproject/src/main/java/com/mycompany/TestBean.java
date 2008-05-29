package com.mycompany;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TestBean {

	private Provider<String> stringProvider;

	public Provider<String> getStringProvider() {
		return stringProvider;
	}

	@Inject
	public void setStringProvider(Provider<String> stringProvider) {
		this.stringProvider = stringProvider;
	}

}
