package salve.depend;

import junit.framework.TestCase;
import salve.BytecodeLoader;
import salve.loader.ClassLoaderLoader;

public class AnalyzerTest extends TestCase {
	public void testAnalyzer() {
		BytecodeLoader loader = new ClassLoaderLoader(AnalyzerTest.class.getClassLoader());
		ClassAnalyzer analyzer = null;

		analyzer = new ClassAnalyzer(loader, "salve/depend/BeanWithoutDependencies");
		assertTrue(!analyzer.shouldInstrument());

		analyzer = new ClassAnalyzer(loader, "salve/depend/Bean");
		assertTrue(analyzer.shouldInstrument());
	}
}
