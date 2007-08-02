package salve.contract;

import org.junit.Assert;

import salve.loader.BytecodePool;

public class AbstractContractInstrumentorTest extends Assert {
	private static final ClassLoader CL = AbstractContractInstrumentorTest.class
			.getClassLoader();
	private static final ContractInstrumentor INST = new ContractInstrumentor();

	protected final Object create(String beanName) throws Exception {
		return instrument(beanName).newInstance();
	}

	protected final Class<?> instrument(String beanName) throws Exception {
		return new BytecodePool().addLoaderFor(CL).instrumentIntoClass(
				getClass().getName().replace(".", "/") + "$" + beanName, INST);
	}

}
