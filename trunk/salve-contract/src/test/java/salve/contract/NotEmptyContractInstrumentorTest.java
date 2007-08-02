package salve.contract;

import org.junit.Test;

import salve.InstrumentationException;

public class NotEmptyContractInstrumentorTest extends
		AbstractContractInstrumentorTest {
	@Test
	public void testArgumentTypeErrorChecking() throws Exception {
		try {
			create("NonStringParameterBean");
			fail("Expected error instrumenting non-string notempty argument");
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				throw e;
			}
		}
	}

	@Test
	public void testNotEmpty() throws Exception {
		Bean bean = (Bean) create("Bean");
		final String token = new String("foo");

		assertTrue(token == bean.test(token));
		assertTrue(token == bean.test(null, token, null));

		try {
			bean.test(null, null, null);
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
			// System.out.println(e.getMessage());
		}

		try {
			bean.test(null, "  ", null);
			fail("Expected " + IllegalArgumentException.class.getName());
		} catch (IllegalArgumentException e) {
			// expected
			// System.out.println(e.getMessage());
		}

		try {
			bean.test(null, Bean.NULL, null);
			fail("Expected " + IllegalStateException.class.getName());
		} catch (IllegalStateException e) {
			// expected
			// System.out.println(e.getMessage());
		}

	}

	@Test
	public void testReturnTypeErrorChecking() throws Exception {

		try {
			create("VoidReturnBean");
			fail("Expected error instrumenting notempty method with non-string return type");
		} catch (InstrumentationException e) {
			if (!(e.getCause() instanceof IllegalAnnotationUseException)) {
				throw e;
			}
		}

	}

	public static class Bean {
		public static final String NULL = new String("not-empty");

		public Object test(Object arg1) {
			return arg1;
		}

		@NotEmpty
		public String test(Object arg1, @NotEmpty
		String arg2, Object arg3) {
			return arg2 == NULL ? null : arg2;
		}
	}

	public static class NonStringParameterBean {
		public void testEmptyNull(@NotEmpty
		int a) {

		}
	}

	public static class VoidReturnBean {
		@NotEmpty
		public void test() {
		}
	}

}
