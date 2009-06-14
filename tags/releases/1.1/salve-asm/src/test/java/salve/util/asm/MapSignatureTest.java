package salve.util.asm;

import org.junit.Assert;
import org.junit.Test;

import salve.util.asm.AsmUtil.Pair;

public class MapSignatureTest {
	@Test
	public void test() {
		String str = "Ljava/util/Map<Ljava/lang/String;Lsalve/contract/pe/map/Member;>;";
		Pair<String, String> types = AsmUtil.parseMapTypesFromSignature(str);
		Assert.assertEquals("java/lang/String", types.getKey());
		Assert.assertEquals("salve/contract/pe/map/Member", types.getValue());

		// str =
		// "Ljava/util/Map<Ljava/lang/String;Ljava/util/List<Lsalve/expr/checker/TortureTest$Person;>;>;";
		// SignatureReader reader = new SignatureReader(str);
		// reader.accept(new TracingSignatureVisitorAdapter());
		//
		// types = AsmUtil.parseMapTypesFromSignature(str);

	}
}
