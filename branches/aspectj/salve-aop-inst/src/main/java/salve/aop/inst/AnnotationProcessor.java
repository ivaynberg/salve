package salve.aop.inst;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.MethodVisitor;

public interface AnnotationProcessor
{
    AnnotationVisitor filter(MethodVisitor mv, String desc, boolean visible);

    boolean filter(String desc);
}
