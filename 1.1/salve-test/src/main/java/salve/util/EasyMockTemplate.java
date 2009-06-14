package salve.util;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;

/**
 * Simple template class for easy mock tests that seperates setting up
 * expections from testing them.
 * 
 * @author igor.vaynberg
 * 
 */
public abstract class EasyMockTemplate {

	private final List<Object> mocks = new ArrayList<Object>();

	public EasyMockTemplate(Object... mocks) {
		if (mocks == null || mocks.length == 0) {
			throw new IllegalArgumentException("Argument mocks cannot be null or empty");
		}
		for (Object mock : mocks) {
			if (mock == null) {
				throw new IllegalArgumentException("Argument mock cannot contain a null entry");
			}
			this.mocks.add(mock);
		}
	}

	private void resetMocks() {
		for (Object mock : mocks) {
			EasyMock.reset(mock);
		}
	}

	protected abstract void setupExpectations();

	public final void test() throws Exception {
		resetMocks();
		setupExpectations();
		for (Object mock : mocks) {
			EasyMock.replay(mock);
		}
		testExpectations();
		for (Object mock : mocks) {
			EasyMock.verify(mock);
		}
		resetMocks();
	}

	protected abstract void testExpectations() throws Exception;

}
