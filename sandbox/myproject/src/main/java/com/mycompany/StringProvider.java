package com.mycompany;

import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class StringProvider implements Provider<String> {
	public static final String STRING = "hello world";

	public String get() {
		return STRING;
	}

}