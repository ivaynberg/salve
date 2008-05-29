package com.mycompany;

import junit.framework.TestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceModuleTest extends TestCase {

	public void testModule() {

		Module module = new GuiceModule();
		Injector injector = Guice.createInjector(module);

		TestBean bean = new TestBean();
		injector.injectMembers(bean);
		assertNotNull(bean.getStringProvider());
		assertEquals(StringProvider.STRING, bean.getStringProvider().get());

	}
}
