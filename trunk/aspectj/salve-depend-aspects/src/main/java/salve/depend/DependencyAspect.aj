package salve.depend;

import java.lang.reflect.Field;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.FieldSignature;

privileged public aspect DependencyAspect
{
    public pointcut readNonStatic(Object o, salve.depend.Dependency annot):get(@salve.depend.Dependency * *.*)&&this(o)&&@annotation(annot);

    public pointcut readStatic(salve.depend.Dependency annot):get(@salve.depend.Dependency static * *.*)&&@annotation(annot);

    public pointcut readMissingTransient():get(@salve.depend.Dependency(strategy=salve.depend.InstrumentationStrategy.REMOVE_FIELD) !transient !static * *.*);

    public pointcut writeIntoRemoved():set(@salve.depend.Dependency(strategy=salve.depend.InstrumentationStrategy.REMOVE_FIELD) * *.*);

    declare error:readMissingTransient():"Field must be transient";


    declare error:writeIntoRemoved():"Cannot write into the dependency field";
    

    Object around(Object o, Dependency annot): readNonStatic(o, annot){
        return handleRead(o, annot, thisJoinPoint.getSignature(), proceed(o, annot));
    }

    Object around(Dependency annot): readStatic(annot){
        return handleRead(null, annot, thisJoinPoint.getSignature(), proceed(annot));
    }

    protected Object handleRead(Object instance, Dependency annot, Signature signature, Object value)
    {
        final Dependency dep = annot;
        final InstrumentationStrategy strat = dep.strategy();
        try
        {
            switch (strat)
            {
                case INJECT_FIELD :
                    if (value == null)
                    {
                        final Field field = getField(signature);
                        final Key key = new FieldKey(field);
                        value = DependencyLibrary.locate(key);
                        field.set(instance, value);
                    }
                    return value;
                case REMOVE_FIELD :
                    final Field field = getField(signature);
                    final Key key = new FieldKey(field);
                    return DependencyLibrary.locate(key);

            }
            throw new RuntimeException();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    private Field getField(Signature signature)
    {
        try
        {
            Field field = ((FieldSignature)signature).getField();
            field.setAccessible(true);
            return field;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }


    }

}
