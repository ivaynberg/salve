package salve.depend.spring.txn;

import java.util.Set;

import salve.aop.inst.AnnotationProcessor;
import salve.aop.inst.Aspect;
import salve.aop.inst.AspectDiscoveryStrategy;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.MethodVisitor;
import salve.model.MethodModel;

public class TransactionalAnnotAspectDiscoveryStrategy implements AspectDiscoveryStrategy
{
    static final String TRANSACTIONAL_DESC = "Lorg/springframework/transaction/annotation/Transactional;";
    static final String SPRINGTRANSACTIONAL_DESC = "Lsalve/depend/spring/txn/SpringTransactional;";

    public void discover(MethodModel method, Set<Aspect> aspects)
    {
        if (method.getAnnot("Lorg/springframework/transaction/annotation/Transactional;") != null)
        {
            Aspect aspect = new Aspect("salve/depend/spring/txn/TransactionalAdvice", "transact");
            aspect.setAnnotationProcessor(new AnnotationProcessor()
            {

                public AnnotationVisitor filter(MethodVisitor mv, String desc, boolean visible)
                {
                    return mv.visitAnnotation(SPRINGTRANSACTIONAL_DESC, true);
                }

                public boolean filter(String desc)
                {
                    return TRANSACTIONAL_DESC.equals(desc);
                }

            });
            aspects.add(aspect);
        }

    }

}
