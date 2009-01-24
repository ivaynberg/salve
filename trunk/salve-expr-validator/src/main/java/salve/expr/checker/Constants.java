package salve.expr.checker;

import salve.asmlib.Type;

public interface Constants
{
    static final Type PE = Type.getType("Lsalve/expr/PE;");
    static final Arg[] PE_INIT = new Arg[] { Arg.TYPE, Arg.EXPRESSION, Arg.MODE };
}
