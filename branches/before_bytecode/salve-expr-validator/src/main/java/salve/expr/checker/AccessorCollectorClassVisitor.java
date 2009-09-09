/**
 * 
 */
package salve.expr.checker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import salve.asmlib.FieldVisitor;
import salve.asmlib.Method;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.util.asm.ClassVisitorAdapter;

final class AccessorCollectorClassVisitor extends ClassVisitorAdapter
{

    private final List<Accessor> accessors = new ArrayList<Accessor>(1);

    private final String part;
    private final String getterName;
    private final String booleanGetterName;
    private final String setterName;
    private final String mode;

    public AccessorCollectorClassVisitor(String part, String mode)
    {
        this.part = part;
        this.mode = mode;
        final String capped = Character.toUpperCase(part.charAt(0)) + part.substring(1);

        getterName = "get" + capped;
        setterName = "set" + capped;
        booleanGetterName = "is" + capped;
    }

    public List<Accessor> getAccessors()
    {
        return Collections.unmodifiableList(accessors);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces)
    {
        accessors.clear();
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature,
            Object value)
    {
        if (name.equals(part))
        {
            final Accessor accessor = new Accessor(Accessor.Type.FIELD, name, desc, signature);
            accessors.add(accessor);
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions)
    {
        if (mode.contains("r") && name.equals(getterName))
        {
            Method method = new Method(name, desc);
            // make sure this getter has no parameters
            if (method.getArgumentTypes().length == 0)
            {
                final Accessor accessor = new Accessor(Accessor.Type.GETTER, name, desc, signature);
                accessors.add(accessor);
            }
        }
        else if (mode.contains("r") && name.equals(booleanGetterName))
        {
            Method method = new Method(name, desc);
            // make sure this getter has no parameters
            if (method.getArgumentTypes().length == 0)
            {
                // make sure it returns a boolean
                final Type ret = method.getReturnType();
                if (ret.equals(Type.BOOLEAN_TYPE) ||
                        "java/lang/Boolean".equals(ret.getInternalName()))
                {
                    final Accessor accessor = new Accessor(Accessor.Type.GETTER, name, desc,
                            signature);
                    accessors.add(accessor);
                }
            }

        }
        else if (mode.contains("w") && name.equals(setterName))
        {
            Method method = new Method(name, desc);
            if (method.getReturnType().equals(Type.VOID_TYPE) &&
                    method.getArgumentTypes().length == 1)
            {
                final Accessor accessor = new Accessor(Accessor.Type.SETTER, name, desc, signature);
                accessors.add(accessor);
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

}