package salve.expr;

/**
 * Represents a property expression. Property expressions are strings that navigate object
 * structures such as fields and getters/setters. Property expressions are defined by specifying the
 * starting class and a string path that navigates it, eg <code>new Pe(Person.class, 'address.city');</code>.
 * 
 * @author igor.vaynberg
 */
public class Pe
{
    private final String path;

    /**
     * Constructor
     * 
     * @param root
     *            starting type
     * @param path
     *            property path
     */
    public Pe(Class< ? > root, String path)
    {
        this.path = path;
    }

    /**
     * Constructor to be used for property expressions of form:
     * <code>new Pe(this, "foo.bar");</code>
     * 
     * @param object
     *            this parameter *MUST BE* '<code>this</code>'
     * @param path
     *            property path
     */
    public Pe(Object o, String path)
    {
        this.path = path;
    }

    /**
     * Constructor
     * 
     * @param root
     *            starting type
     * @param path
     *            property path
     * @param mode
     *            epxression mode. Can be 'r' - read, 'w' - write, 'rw' - read/write. Read
     *            expressions must end with an available field or getter. Write expessions must end
     *            with an available field or setter.
     */
    public Pe(Class< ? > root, String path, String mode)
    {
        this.path = path;
    }

    @Override
    public String toString()
    {
        return path;
    }

}
