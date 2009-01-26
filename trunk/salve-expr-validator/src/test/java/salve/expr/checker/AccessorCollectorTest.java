package salve.expr.checker;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class AccessorCollectorTest extends AbstractTest
{
    private static class AbstractBean
    {
        public void setField1(int val)
        {

        }
    }

    private static class Bean extends AbstractBean
    {
        public int getField1()
        {
            return 0;
        }
    }

    @Test
    public void test()
    {
        AccessorCollector ac = new AccessorCollector(getLoader());
        Map<Accessor.Type, Accessor> map = ac.collect(Bean.class.getName().replace(".", "/"),
            "field1", "rw", null, null);

        Assert.assertEquals(2, map.size());
        Assert.assertNotNull(map.get(Accessor.Type.SETTER));
        Assert.assertNotNull(map.get(Accessor.Type.GETTER));

    }
}
