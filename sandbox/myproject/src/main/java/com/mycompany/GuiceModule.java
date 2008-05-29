/**
 * 
 */
package com.mycompany;

import com.google.inject.AbstractModule;

final class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Service.class).toInstance(new ServiceImpl());
		bind(String.class).toProvider(StringProvider.class);
	}
}