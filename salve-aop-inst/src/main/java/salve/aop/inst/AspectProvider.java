package salve.aop.inst;

import java.util.Collection;

import salve.model.MethodModel;

public interface AspectProvider
{
    Collection<Aspect> getAspects(MethodModel method);
}
