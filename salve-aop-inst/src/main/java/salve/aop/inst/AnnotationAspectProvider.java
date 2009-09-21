package salve.aop.inst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import salve.model.CtAnnotation;
import salve.model.CtClass;
import salve.model.CtMethod;
import salve.model.CtProject;
import salve.model.CtAnnotation.ValueField;


public class AnnotationAspectProvider implements AspectProvider
{

    public Collection<Aspect> getAspects(CtMethod mm)
    {
        List<Aspect> aspects = new ArrayList<Aspect>(1);
        boolean inheritedOnly = false;
        while (mm != null)
        {
            List<CtAnnotation> annots = new ArrayList<CtAnnotation>();
            annots.addAll(mm.getAnnotations());
            for (int i = 0; i < mm.getArgCount(); i++)
            {
                annots.addAll(mm.getArgAnnots(i));
            }

            for (CtAnnotation annot : annots)
            {
                CtProject pm = mm.getClassModel().getProject();
                CtClass acm = pm.getClass(annot.getName());

                if (acm != null)
                {
                    CtAnnotation aspectAnnot = acm.getAnnotation("Lsalve/aop/MethodAdvice;");
                    boolean inherited = acm.getAnnotation("Ljava/lang/annotation/Inherited;") != null;
                    if (aspectAnnot != null && (!inheritedOnly || (inheritedOnly && inherited)))
                    {
                        final String ic = ((ValueField)aspectAnnot.getField("adviceClass"))
                                .getValue().toString();
                        final String im = ((ValueField)aspectAnnot.getField("adviceMethod"))
                                .getValue().toString();


                        aspects.add(new Aspect(ic, im));
                    }
                }
            }
            // mm = mm.getSuper();
            // inheritedOnly = true;
            mm = null;
        }
        return aspects;
    }
}
