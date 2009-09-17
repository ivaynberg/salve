package salve.expr.checker;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import salve.asmlib.Type;
import salve.expr.scanner.Expression;

@Ignore
public class TortureTest extends AbstractTest
{
    private static class Person
    {
        public String name;
    }

    private static class AddressBook
    {
        Map<String, List<Person>> people;
    }

    @Test
    public void test()
    {
        ExpressionChecker checker = new ExpressionChecker(getLoader());
        try
        {
            checker.validate(new Expression(Type.getType(AddressBook.class),
                    "people.father.0.name", "r"));
        }
        catch (CheckerException e)
        {
            Assert.fail(e.getMessage());
        }
    }
}
