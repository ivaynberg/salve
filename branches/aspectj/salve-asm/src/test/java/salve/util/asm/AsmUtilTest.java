package salve.util.asm;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import salve.asmlib.Opcodes;

public class AsmUtilTest implements Opcodes {

	@Test
	public void testSetPermission() {
		int access = ACC_STATIC | ACC_PRIVATE;
		access = AsmUtil.setPermission(access, ACC_PROTECTED);
		assertTrue((access & ACC_STATIC) > 0);
		assertTrue((access & ACC_PROTECTED) > 0);
		assertTrue((access & ACC_PRIVATE) == 0);
		assertTrue((access & ACC_PUBLIC) == 0);

		access = ACC_STATIC | ACC_PROTECTED;
		access = AsmUtil.setPermission(access, ACC_PRIVATE);
		assertTrue((access & ACC_STATIC) > 0);
		assertTrue((access & ACC_PROTECTED) == 0);
		assertTrue((access & ACC_PRIVATE) > 0);
		assertTrue((access & ACC_PUBLIC) == 0);

	}
}
