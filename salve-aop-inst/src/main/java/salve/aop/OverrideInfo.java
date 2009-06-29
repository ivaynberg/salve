package salve.aop;

import salve.asmlib.Type;

class OverrideInfo
{
    private String type;
    private String name;
    private String desc;
    private String rootDelegateMethodName;

    OverrideInfo()
    {

    }

    public OverrideInfo(String type, String name, String desc, String rootDelegateMethodName)
    {
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.rootDelegateMethodName = rootDelegateMethodName;
    }

    public String getType()
    {
        return type;
    }

    public String getMethod()
    {
        return name;
    }

    public String getDesc()
    {
        return desc;
    }

    public String getRootDelegateMethodName()
    {
        return rootDelegateMethodName;
    }


}
