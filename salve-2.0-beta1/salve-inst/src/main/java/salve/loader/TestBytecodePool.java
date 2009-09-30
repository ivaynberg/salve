package salve.loader;

import java.util.HashMap;
import java.util.Map;

import salve.Bytecode;
import salve.BytecodeLoader;
import salve.InstrumentationContext;
import salve.Instrumentor;
import salve.Scope;
import salve.util.ClassesUtil;
import salve.util.TestLogger;

/**
 * Bytecode pool that can save bytecode into memory and load it on subsequent
 * requsets. Useful for testing instrumentors where a class needs to be
 * instrumented without being persisted to disk.
 * 
 * @author igor.vaynberg
 * 
 */
public class TestBytecodePool extends BytecodePool {
	private final Map<String, Bytecode> saved = new HashMap<String, Bytecode>();

	/**
	 * Constructor
	 * 
	 * @param loader
	 */
	public TestBytecodePool(BytecodeLoader loader) {
		this(Scope.ALL, loader);
	}

	/**
	 * Constructor
	 * 
	 * @param cl
	 */
	public TestBytecodePool(ClassLoader cl) {
		this(Scope.ALL, cl);
	}

	/**
	 * Constructor
	 * 
	 * @param scope
	 */
	public TestBytecodePool(Scope scope) {
		super(scope);
	}

	/**
	 * Constructor
	 * 
	 * @param scope
	 * @param loader
	 */
	public TestBytecodePool(Scope scope, BytecodeLoader loader) {
		super(scope);
		addLoader(loader);
	}

	/**
	 * Constructor
	 * 
	 * @param scope
	 * @param cl
	 */
	public TestBytecodePool(Scope scope, ClassLoader cl) {
		super(scope);
		addLoaderFor(cl);
	}

	/**
	 * Instruments a class
	 * 
	 * @param className
	 *            binary class name
	 * @param inst
	 *            bytecode instrumentor
	 * @return instrumented bytecode
	 * @throws Exception
	 */
	public byte[] instrumentIntoBytecode(String className, Instrumentor inst) throws Exception {
		InstrumentationContext ctx = new InstrumentationContext(this, scope, model, TestLogger.INSTANCE);
		byte[] bytecode = inst.instrument(className, ctx);
		save(className, bytecode);
		return bytecode;
	}

	/**
	 * Instruments a class and loads the result into a {@link Class} object
	 * 
	 * @param className
	 *            binary class name
	 * @param inst
	 *            instrumentor
	 * @return created {@link Class} object
	 * @throws Exception
	 */
	public Class<?> instrumentIntoClass(String className, Instrumentor inst) throws Exception {
		instrumentIntoBytecode(className, inst);
		return loadClass(className);
	}

	@Override
	public Bytecode loadBytecode(String className) {
		Bytecode bytecode = saved.get(className);
		if (bytecode == null) {
			bytecode = super.loadBytecode(className);
		}
		return bytecode;
	}

	/**
	 * Saves bytecode in the pool
	 * 
	 * @param className
	 *            binary class name
	 * @param bytecode
	 *            bytecode
	 */
	public void save(String className, byte[] bytecode) {
		ClassesUtil.checkClassNameArg(className);
		if (bytecode == null) {
			throw new IllegalArgumentException("Argument `bytecode` cannot be null");
		}
		saved.put(className, new Bytecode(className, bytecode, null));
	}

}
