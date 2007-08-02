package salve.util.asm;

import salve.org.objectweb.asm.Type;

public class AsmUtil {
	private static final String DOUBLEDESC = "Ljava/lang/Double;";
	private static final String FLOATDESC = "Ljava/lang/Float;";
	private static final String LONGDESC = "Ljava/lang/Long;";
	private static final String INTEGERDESC = "Ljava/lang/Integer;";
	private static final String SHORTDESC = "Ljava/lang/Short;";
	private static final String BYTEDESC = "Ljava/lang/Byte;";
	private static final String BOOLEANDESC = "Ljava/lang/Boolean;";
	private static final String CHARDESC = "Ljava/lang/Character;";

	private AsmUtil() {

	}

	public static boolean isDouble(Type type) {
		return Type.DOUBLE == type.getSort() || "Ljava/lang/Double;".equals(type.getDescriptor());
	}

	public static boolean isFloat(Type type) {
		return Type.FLOAT == type.getSort() || "Ljava/lang/Float;".equals(type.getDescriptor());
	}

	public static boolean isLong(Type type) {
		return Type.LONG == type.getSort() || "Ljava/lang/Long;".equals(type.getDescriptor());
	}

	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY;
	}

	public static Type toPrimitive(Type type) {
		int sort = type.getSort();
		if (sort == Type.ARRAY) {
			throw new IllegalArgumentException("Type `" + type.toString() + "` does not have a primitive counterpart");
		}
		if (sort == Type.OBJECT) {
			String desc = type.getDescriptor();
			if (DOUBLEDESC.equals(desc)) {
				return Type.DOUBLE_TYPE;
			} else if (FLOATDESC.equals(desc)) {
				return Type.FLOAT_TYPE;
			} else if (FLOATDESC.equals(desc)) {
				return Type.FLOAT_TYPE;
			} else if (INTEGERDESC.equals(desc)) {
				return Type.INT_TYPE;
			} else if (SHORTDESC.equals(desc)) {
				return Type.SHORT_TYPE;
			} else if (BYTEDESC.equals(desc)) {
				return Type.BYTE_TYPE;
			} else if (BOOLEANDESC.equals(desc)) {
				return Type.BOOLEAN_TYPE;
			} else if (CHARDESC.equals(desc)) {
				return Type.CHAR_TYPE;
			} else {
				throw new IllegalStateException("Cannot convert type `" + desc + "` to its primitive counterpart`");
			}
		} else {
			return type;
		}

	}

}
