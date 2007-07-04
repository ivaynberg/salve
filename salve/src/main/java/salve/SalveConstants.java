package salve;

public class SalveConstants {
	private SalveConstants() {

	}

	private static final String KEY_FIELD_PREFIX = "__key$";
	private static final String PROXY_CLASS_SUFFIX = "$SalveProxy";

	public static final String keyFieldName(String fieldName) {
		return KEY_FIELD_PREFIX + fieldName;
	}

	public static final String proxyClassName(String type) {
		return type + PROXY_CLASS_SUFFIX;
	}
}
