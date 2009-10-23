package salve;


public class NotEmptyCharSequenceContractAspectTest extends AbstractContractAspectTest
{

    public void testArguments() throws Throwable
    {
        executeArgumentTestHarness(new NotEmptyCharSequenceTestBean(), "test", 10, "a", null);
        executeArgumentTestHarness(new NotEmptyCharSequenceTestBean(), "test", 10, "a", "");
        executeArgumentTestHarness(new NotEmptyCharSequenceTestBean(), "test", 10, "a", "    ");
    }

    public void testConstructors() throws Throwable
    {
        executeArgumentTestHarness(new NotEmptyCharSequenceTestBean(), null, 10, "a", null);
        executeArgumentTestHarness(new NotEmptyCharSequenceTestBean(), null, 10, "a", "");
        executeArgumentTestHarness(new NotEmptyCharSequenceTestBean(), null, 10, "a", "    ");
    }

}
