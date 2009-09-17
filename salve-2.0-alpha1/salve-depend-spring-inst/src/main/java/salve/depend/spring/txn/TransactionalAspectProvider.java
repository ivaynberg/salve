package salve.depend.spring.txn;

import java.util.Collection;
import java.util.Collections;

import salve.aop.inst.AnnotationProcessor;
import salve.aop.inst.Aspect;
import salve.aop.inst.AspectProvider;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.MethodVisitor;
import salve.model.AnnotationModel;
import salve.model.ClassModel;
import salve.model.MethodModel;

public class TransactionalAspectProvider implements AspectProvider
{
    static final String TRANSACTIONAL_DESC = "Lorg/springframework/transaction/annotation/Transactional;";
    static final String SPRINGTRANSACTIONAL_DESC = "Lsalve/depend/spring/txn/SpringTransactional;";

    public Collection<Aspect> getAspects(MethodModel method)
    {
        if (method == null)
        {
            throw new IllegalArgumentException("Argument `method` cannot be null");
        }
        AnnotationModel methodAnnot = method.getAnnot(TRANSACTIONAL_DESC);
        ClassModel classModel = method.getClassModel();
        AnnotationModel classAnnot = classModel.getAnnotation(TRANSACTIONAL_DESC);
        if (methodAnnot != null ||
                classAnnot != null)
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
            return Collections.singletonList(aspect);
        }

        return null;
    }
}
