package salve.depend;

import junit.framework.TestCase;
import salve.BytecodeLoader;
import salve.InstrumentationContext;
import salve.Scope;
import salve.loader.ClassLoaderLoader;
import salve.model.CtProject;
import salve.monitor.NoopMonitor;

public class AnalyzerTest extends TestCase {
	public void testAnalyzer() {
		BytecodeLoader loader = new ClassLoaderLoader(AnalyzerTest.class.getClassLoader());
		ClassAnalyzer analyzer = null;

		InstrumentationContext ctx = new InstrumentationContext(loader, NoopMonitor.INSTANCE, Scope.ALL,
				new CtProject().setLoader(loader));

		analyzer = new ClassAnalyzer("salve/depend/BeanWithoutDependencies", ctx);
		assertTrue(!analyzer.shouldInstrument());

		analyzer = new ClassAnalyzer("salve/depend/Bean", ctx);
		assertTrue(analyzer.shouldInstrument());
	}
}
