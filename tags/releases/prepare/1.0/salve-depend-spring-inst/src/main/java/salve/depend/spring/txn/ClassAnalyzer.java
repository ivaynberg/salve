package salve.depend.spring.txn;

public interface ClassAnalyzer {
	public boolean shouldInstrument(int access, String name, String desc,
			String sig, String[] exceptions);
}
