package salve.aop.inst;

public class Aspect
{
    private String clazz;
    private String method;
    private AnnotationProcessor annotationProcessor;

    public Aspect(String clazz, String method)
    {
        this.clazz = clazz;
        this.method = method;
    }

    public AnnotationProcessor getAnnotationProcessor()
    {
        return annotationProcessor;
    }

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor)
    {
        this.annotationProcessor = annotationProcessor;
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Aspect other = (Aspect)obj;
        if (clazz == null)
        {
            if (other.clazz != null)
                return false;
        }
        else if (!clazz.equals(other.clazz))
            return false;
        if (method == null)
        {
            if (other.method != null)
                return false;
        }
        else if (!method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "[" + getClass().getSimpleName() + " clazz=" + clazz + ", method=" + method + "]";
    }

//    public String encode()
//    {
//        return clazz + "#" + method;
//    }
//
//    public static Aspect decode(String value)
//    {
//        String[] parts = value.split("#");
//        return new Aspect(parts[0], parts[1]);
//    }

    public String getClazz()
    {
        return clazz;
    }

    public String getMethod()
    {
        return method;
    }
    
    
}