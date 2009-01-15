package salve.expr.inst.locator;

import java.util.Set;

import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;

public class MethodScanner extends MethodAdapter
{

    public MethodScanner(MethodVisitor mv, Set<ExpressionDefinition> definitions)
    {
        super(mv);
        // TODO Auto-generated constructor stub
    }

}
