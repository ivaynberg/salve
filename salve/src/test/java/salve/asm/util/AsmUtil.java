package salve.asm.util;

import salve.org.objectweb.asm.Opcodes;

public class AsmUtil {

	private static final String CLINIT_DESC = "()V";
	private static final String CLINIT_NAME = "<clinit>";

	private AsmUtil() {

	}

	public static boolean isClInitMethod(int access, String name, String desc) {
		return (access & Opcodes.ACC_STATIC) != 0 && CLINIT_NAME.equals(name)
				&& CLINIT_DESC.equals(desc);
	}
}
