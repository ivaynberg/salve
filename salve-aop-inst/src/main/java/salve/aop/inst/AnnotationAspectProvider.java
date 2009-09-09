package salve.aop.inst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import salve.model.AnnotationModel;
import salve.model.ClassModel;
import salve.model.MethodModel;
import salve.model.AnnotationModel.ValueField;


public class AnnotationAspectProvider implements AspectProvider
{

    public Collection<Aspect> getAspects(MethodModel mm)
    {
        List<Aspect> aspects = new ArrayList<Aspect>(1);
        boolean inheritedOnly = false;
        while (mm != null)
        {
            List<AnnotationModel> annots = new ArrayList<AnnotationModel>();
            annots.addAll(mm.getAnnotations());
            for (int i = 0; i < mm.getArgCount(); i++)
            {
                annots.addAll(mm.getArgAnnots(i));
            }

            for (AnnotationModel annot : annots)
            {
                ClassModel acm = mm.getClassModel().getProject().getClass(annot.getName());
                AnnotationModel aspectAnnot = acm.getAnnotation("Lsalve/aop/MethodAdvice;");
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
            mm = mm.getSuper();
            inheritedOnly = true;
        }
        return aspects;
    }

}
