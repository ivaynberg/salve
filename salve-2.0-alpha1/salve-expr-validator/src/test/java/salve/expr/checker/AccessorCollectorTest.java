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

    private static class BooleanBean
    {
        public boolean isCool()
        {
            return true;
        }

        public Boolean isNotCool()
        {
            return false;
        }

        public String isNotEvenGetter()
        {
            return null;
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

    @Test
    public void booleanGettersStartingWithIs()
    {
        AccessorCollector ac = new AccessorCollector(getLoader());
        // test boolean
        Map<Accessor.Type, Accessor> map = ac.collect(
                BooleanBean.class.getName().replace(".", "/"), "cool", "r", null, null);
        Assert.assertEquals(1, map.size());

        // test Boolean
        map = ac.collect(BooleanBean.class.getName().replace(".", "/"), "notCool", "r", null, null);
        Assert.assertEquals(1, map.size());

        // test invalid
        map = ac.collect(BooleanBean.class.getName().replace(".", "/"), "notEvenGetter", "r", null, null);
        Assert.assertEquals(0, map.size());
        
    }
}
