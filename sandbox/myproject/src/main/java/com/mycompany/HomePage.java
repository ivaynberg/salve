package com.mycompany;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import salve.depend.Dependency;

import com.google.inject.Provider;

/**
 * Homepage
 */
public class HomePage extends WebPage {

	private static final long serialVersionUID = 1L;

	@Dependency
	private Service service;

	@Dependency
	private Provider<String> stringProvider;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage(final PageParameters parameters) {
		add(new Label("message", service.getHelloText() + " | "
				+ stringProvider.get()));
	}

}
