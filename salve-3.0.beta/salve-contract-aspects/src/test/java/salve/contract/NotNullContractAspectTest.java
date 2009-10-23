package salve.contract;


public class NotNullContractAspectTest extends AbstractContractAspectTest
{

    public void testArguments() throws Throwable
    {
        executeArgumentTestHarness(new NotNullTestBean(), "test", 10, "", null);
    }

    public void testConstructors() throws Throwable
    {
        executeArgumentTestHarness(new NotNullTestBean(), null, 10, "", null);
    }


}
