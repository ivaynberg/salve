package salve.config;

import java.util.HashSet;
import java.util.Set;

public class Package
{
    private String name;
    private Set<Instrumentor> instrumentors = new HashSet<Instrumentor>();


    public Set<Instrumentor> getInstrumentors()
    {
        return instrumentors;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
