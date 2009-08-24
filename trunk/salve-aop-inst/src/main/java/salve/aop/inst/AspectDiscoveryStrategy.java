package salve.aop.inst;

import java.util.Set;

import salve.model.MethodModel;

public interface AspectDiscoveryStrategy
{
    void discover(MethodModel method, Set<Aspect> aspects);
}
