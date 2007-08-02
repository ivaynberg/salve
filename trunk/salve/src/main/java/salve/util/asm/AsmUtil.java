package salve.util.asm;

import salve.org.objectweb.asm.Type;

public class AsmUtil {
	private AsmUtil() {

	}

	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY;
	}
}
