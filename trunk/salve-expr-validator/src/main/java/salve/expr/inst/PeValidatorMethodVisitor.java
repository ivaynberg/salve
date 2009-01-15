package salve.expr.inst;

import salve.InstrumentationContext;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;

public class PeValidatorMethodVisitor extends ExpressionUseLocator
{
    final PeValidator validator;

    public PeValidatorMethodVisitor(Type pe, Arg[] constructor, Type owner,
            InstrumentationContext ctx, MethodVisitor mv)
    {
        super(pe, constructor, owner, mv);
        validator = new PeValidator(ctx);
    }

    @Override
    protected void validatePeInstantiation(PeDefinition data)
    {
        validator.validate(data);
    }
}
