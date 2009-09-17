package salve.model;

public interface UpdateListener {
	public static enum Action {
		REMOVE, LEAVE
	}

	public Action updated();;
}
