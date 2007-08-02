package salve.contract;

public class OMCSInstrumentorTest extends AbstractContractInstrumentorTest {

	public static class BaseBean {
		public void test1() {
		}

		public void test2() {

		}
	}

	public static class Bean extends BaseBean {
		@Override
		public void test1() {
		}

		@Override
		public void test2() {
			super.test2();
		}
	}

}
