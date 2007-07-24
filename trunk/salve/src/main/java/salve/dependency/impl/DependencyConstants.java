package salve.dependency.impl;

import javassist.CtMethod;
import javassist.NotFoundException;

public class DependencyConstants {
	public static final String KEY_FIELD_PREFIX = "_salvedepkey$";
	public static final String LOCATOR_METHOD_PREFIX = "_salveloc$";

	private static final String[] PARAMS_CACHE = { "", "$1", "$1,$2",
			"$1,$2,$3", "$1,$2,$3,$4", "$1,$2,$3,$4,$5", "$1,$2,$3,$4,$5,$6",
			"$1,$2,$3,$4,$5,$6,$7", "$1,$2,$3,$4,$5,$6,$7,$8",
			"$1,$2,$3,$4,$5,$6,$7,$8,$9", "$1,$2,$3,$4,$5,$6,$7,$8,$9,$10" };

	private static final String PROXY_CLASS_SUFFIX = "$SalveProxy";

	private DependencyConstants() {

	}

	public static void insertParamsList(CtMethod method, StringBuilder code)
			throws NotFoundException {
		final int params = method.getParameterTypes().length;
		if (params < PARAMS_CACHE.length) {
			code.append(PARAMS_CACHE[params]);
		} else {
			code.append(PARAMS_CACHE[PARAMS_CACHE.length - 1]);
			for (int i = PARAMS_CACHE.length; i < params; i++) {
				code.append(",$").append(i + 1);
			}
		}
	}

	public static final String keyFieldName(String fieldName) {
		return KEY_FIELD_PREFIX + fieldName;
	}

	public static final String proxyClassName(String type) {
		return type + PROXY_CLASS_SUFFIX;
	}
}
