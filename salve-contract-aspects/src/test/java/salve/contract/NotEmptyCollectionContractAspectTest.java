package salve.contract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class NotEmptyCollectionContractAspectTest extends AbstractContractAspectTest
{

    public void testArguments() throws Throwable
    {
        List<String> single = Arrays.asList(new String[] { "a" });
        List<String> empty = Arrays.asList(new String[] { });

        executeArgumentTestHarness(new NotEmptyCollectionTestBean(), "test", 10, single, null);
        executeArgumentTestHarness(new NotEmptyCollectionTestBean(), "test", 10, single, empty);
    }

    public void testConstructor() throws Throwable
    {
        List<String> single = Arrays.asList(new String[] { "a" });
        List<String> empty = Arrays.asList(new String[] { });

        executeArgumentTestHarness(new NotEmptyCollectionTestBean(), null, 10, single, null);
        executeArgumentTestHarness(new NotEmptyCollectionTestBean(), null, 10, single, empty);
    }
    
    public void testList()
    {
        new NotEmptyCollectionTestBean().testList(Collections.singletonList("a"));
        try
        {
            new NotEmptyCollectionTestBean().testList(Collections.emptyList());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop
        }
    }

    public void testSet()
    {
        new NotEmptyCollectionTestBean().testSet(Collections.singleton("a"));
        try
        {
            new NotEmptyCollectionTestBean().testSet(Collections.emptySet());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop
        }
    }

}
