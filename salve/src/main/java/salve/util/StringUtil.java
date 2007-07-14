package salve.util;

public class StringUtil {
	private StringUtil() {

	}

	public static String join(String delimiter, Object... items) {
		return joinInto(new StringBuilder(), delimiter, items).toString();
	}

	public static StringBuilder joinInto(StringBuilder builder,
			String delimiter, Object... items) {
		for (int i = 0; i < items.length; i++) {
			builder.append(items[i].toString());
			if (i < items.length - 1) {
				builder.append(delimiter);
			}
		}
		return builder;
	}
}
