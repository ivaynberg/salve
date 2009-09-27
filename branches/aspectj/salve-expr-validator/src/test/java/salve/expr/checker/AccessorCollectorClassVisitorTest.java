package salve.expr.checker;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import salve.asmlib.ClassReader;

public class AccessorCollectorClassVisitorTest extends AbstractTest
{
    private static class Bean
    {
        private int field1;
        private Map<String, Integer> field2;

        public int getField1()
        {
            return field1;
        }

        public void setField2(Map<String, Integer> field2)
        {
            this.field2 = field2;
        }
    }

    /** bean with invalid getters/setters */
    private static class Bean2
    {
        public int getField1(int param)
        {
            return 0;
        }

        public Object getfield1()
        {
            return null;
        }

        public Object setField1(int value)
        {
            return null;
        }

        public void setField1(int value, int value2)
        {

        }
    }

    @Test
    public void test()
    {
        byte[] bytecode = loadBytecode(Bean.class);
        Assert.assertNotNull(bytecode);

        ClassReader reader = new ClassReader(bytecode);

        // validate field1 has field and getter accessors
        AccessorCollectorClassVisitor visitor = new AccessorCollectorClassVisitor("field1", "rw");
        reader.accept(visitor, ClassReader.SKIP_CODE);
        List<Accessor> accessors = visitor.getAccessors();
        Assert.assertEquals(2, accessors.size());
        boolean getter = false, setter = false, field = false;
        for (Accessor accessor : accessors)
        {
            switch (accessor.getType())
            {
                case FIELD :
                    field = true;
                    break;
                case GETTER :
                    getter = true;
                    break;
                case SETTER :
                    setter = true;
                    break;
            }
        }
        Assert.assertTrue(getter);
        Assert.assertFalse(setter);
        Assert.assertTrue(field);

        // validate field2 has field and setter accessors
        visitor = new AccessorCollectorClassVisitor("field2", "rw");
        reader.accept(visitor, ClassReader.SKIP_CODE);
        accessors = visitor.getAccessors();
        Assert.assertEquals(2, accessors.size());
        getter = false;
        setter = false;
        field = false;
        for (Accessor accessor : accessors)
        {
            switch (accessor.getType())
            {
                case FIELD :
                    field = true;
                    break;
                case GETTER :
                    getter = true;
                    break;
                case SETTER :
                    setter = true;
                    break;
            }
        }
        Assert.assertFalse(getter);
        Assert.assertTrue(setter);
        Assert.assertTrue(field);
    }

    @Test
    public void testMode()
    {
        byte[] bytecode = loadBytecode(Bean.class);
        Assert.assertNotNull(bytecode);

        ClassReader reader = new ClassReader(bytecode);

        // validate getField1() getter is ignore in write mode
        AccessorCollectorClassVisitor visitor = new AccessorCollectorClassVisitor("field1", "w");
        reader.accept(visitor, ClassReader.SKIP_CODE);
        List<Accessor> accessors = visitor.getAccessors();
        Assert.assertEquals(1, accessors.size());
        Assert.assertTrue(accessors.get(0).getType().equals(Accessor.Type.FIELD));

        // validate setField2 setter is ignored in read mode
        visitor = new AccessorCollectorClassVisitor("field2", "r");
        reader.accept(visitor, ClassReader.SKIP_CODE);
        accessors = visitor.getAccessors();
        Assert.assertEquals(1, accessors.size());
        Assert.assertTrue(accessors.get(0).getType().equals(Accessor.Type.FIELD));
    }


    @Test
    public void testInvalidGettersAndSetters()
    {
        byte[] bytecode = loadBytecode(Bean2.class);
        Assert.assertNotNull(bytecode);

        ClassReader reader = new ClassReader(bytecode);
        AccessorCollectorClassVisitor visitor = new AccessorCollectorClassVisitor("field1", "rw");
        reader.accept(visitor, ClassReader.SKIP_CODE);
        Assert.assertEquals(0, visitor.getAccessors().size());
    }
}
