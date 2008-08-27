package salve.util.asm;

import org.junit.Assert;
import org.junit.Test;

public class MapSignatureTest {
	@Test
	public void test() {
		String str = "Ljava/util/Map<Ljava/lang/String;Lsalve/contract/pe/map/Member;>;";
		MapSignature sig = new MapSignature();
		sig.parse(str);
		Assert.assertEquals("java/lang/String", sig.getKeyTypeClassName());
		Assert.assertEquals("salve/contract/pe/map/Member", sig.getValueTypeClassName());

	}
}
