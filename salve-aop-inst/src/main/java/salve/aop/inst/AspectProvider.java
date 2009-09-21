package salve.aop.inst;

import java.util.Collection;

import salve.model.CtMethod;

public interface AspectProvider
{
    Collection<Aspect> getAspects(CtMethod method);
}
