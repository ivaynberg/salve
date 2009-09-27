package salve.model;

public interface EvictionListener {
	public void onEviction(String cn, CtProject model);
}
