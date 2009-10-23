package salve.aop.inst;

import salve.asmlib.AnnotationVisitor;

public class AnnotationVisitorAdapter implements AnnotationVisitor
{
    protected final AnnotationVisitor av;

    public AnnotationVisitorAdapter(AnnotationVisitor av)
    {
        this.av = av;
    }

    public void visit(String name, Object value)
    {
        av.visit(name, value);
    }

    public AnnotationVisitor visitAnnotation(String name, String desc)
    {
        return av.visitAnnotation(name, desc);
    }

    public AnnotationVisitor visitArray(String name)
    {
        return av.visitArray(name);
    }

    public void visitEnd()
    {
        av.visitEnd();
    }

    public void visitEnum(String name, String desc, String value)
    {
        av.visitEnum(name, desc, value);
    }

}
