package com.mycompany.issue5;

import salve.depend.DependencyLibrary;
import salve.depend.guice.GuiceBeanLocator;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class Main {
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new Module() {
			public void configure(Binder binder) {
				binder.bind(PersonService.class).to(PersonServiceImpl.class);
			}
		});
		DependencyLibrary.addLocator(new GuiceBeanLocator(injector));
		System.out.println(new Person().getFoo());
	}
}