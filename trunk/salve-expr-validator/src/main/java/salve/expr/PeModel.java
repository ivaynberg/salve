package salve.expr;

public class PeModel
{
    private final String expression;


    // target must be this to validate
    public PeModel(Object target, String expression)
    {
        this.expression = expression;
    }

    public PeModel(Object target, Class< ? > clazz, String expression)
    {
        this.expression = expression;
    }

    @Override
    public String toString()
    {
        return expression;
    }
}
