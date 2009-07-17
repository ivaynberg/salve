package salve.aop;

class Aspect
{
    public String clazz;
    public String method;

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
}