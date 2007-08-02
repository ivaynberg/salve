package salve.contract;

import org.junit.Test;

public class OMIInstrumentorTest extends AbstractContractInstrumentorTest {

	@Test
	public void testProperImplementation() throws Exception {
		final Bean bean = (Bean) create("Bean");
		final Object token = new Object();
		assertTrue(token == bean.test1(token));
		assertTrue(token == bean.test2(token));
		try {
			bean.test3(token);
			fail("Expecting illegal argument exception because super was not called");
		} catch (IllegalStateException e) {

		}
		// test omi annots are inherited from interfaces
		try {
			bean.test4(token);
			fail("Expecting illegal argument exception because super was not called");
		} catch (IllegalStateException e) {

		}
	}

	public static class BaseBean {
		public Object test1(Object o) {
			return o;
		}

		@OverridesMustInvoke
		public Object test2(Object o) {
			return o;
		}

		@OverridesMustInvoke
		public Object test3(Object o) {
			return o;
		}
	}

	public static interface BaseInterface {
		@OverridesMustInvoke
		public Object test4(Object o);
	}

	public static class Bean extends BaseBean implements BaseInterface {
		@Override
		public Object test1(Object o) {
			return o;
		}

		@Override
		public Object test2(Object o) {
			return super.test2(o);
		}

		@Override
		public Object test3(Object o) {
			return o;
		}

		public Object test4(Object o) {
			return null;
		}
	}

}
