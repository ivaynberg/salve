package salve.aop.inst.inline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import salve.InstrumentationContext;
import salve.aop.inst.AnnotationAspectDiscoveryStrategy;
import salve.aop.inst.Aspect;
import salve.aop.inst.AspectDiscoveryStrategy;
import salve.asmlib.AdviceAdapter;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.asmlib.GeneratorAdapter;
import salve.asmlib.Label;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.model.MethodModel;
import salve.util.asm.ClassVisitorAdapter;

public class AopClassInstrumentor2 extends ClassAdapter
{
    private final InstrumentationContext ctx;
    private String owner;

    public AopClassInstrumentor2(InstrumentationContext ctx, ClassWriter writer)
    {
        super(writer);
        this.ctx = ctx;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces)
    {
        this.owner = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions)
    {
        final MethodModel method = ctx.getModel().getClass(owner).getMethod(name, desc);
        Set<Aspect> aspects = gatherAspects(method);
        final Label[] ret = new Label[1];

        final MethodVisitor target = super.visitMethod(access, name, desc, signature, exceptions);


        for (final Aspect aspect : aspects)
        {
            System.out.println("Instrumenting " + method + " with " + aspect);

            byte[] aspectBytecode = ctx.getLoader().loadBytecode(
                    Type.getType(aspect.getClazz()).getInternalName());

            new ClassReader(aspectBytecode).accept(new ClassVisitorAdapter()
            {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                        String signature, String[] exceptions)
                {


                    if (name.equals(aspect.getMethod()))
                    {
                        // FIXME also match on desc
                        return new GeneratorAdapter(target, access, name, desc)
                        {
                            Label original = new Label();

                            public void visitCode()
                            {
                                ret[0] = null;
                                super.visitCode();

                            }

                            public void visitEnd()
                            {
                                mark(original);
                            };

                            public void visitMaxs(int maxStack, int maxLocals)
                            {
                            };

                            public void visitMethodInsn(int opcode, String owner, String name,
                                    String desc)
                            {
                                if (owner.equals("salve/aop/MethodInvocation") &&
                                        name.equals("execute"))
                                {
                                    System.out.println("found aspect original invocation (" +
                                            aspect + "), rerouting");
                                    // super.visitMethodInsn(opcode, owner, name, desc);
                                    pop(); // pops off aload 0 that is part of invocation

                                    goTo(original);
                                    ret[0] = mark();
                                    //returnValue();

                                }
                                else
                                {
                                    super.visitMethodInsn(opcode, owner, name, desc);
                                }

                            };


                        };
                    }
                    else
                    {
                        return super.visitMethod(access, name, desc, signature, exceptions);
                    }
                }
            }, 0);


        }

// MethodVisitor original = super.visitMethod(access, name, desc, signature, exceptions);
// AdviceAdapter adapter = new AdviceAdapter(original, access, name, desc)
// {
//
// };


        return new AdviceAdapter(target, access, name, desc)
        {
            @Override
            protected void onMethodExit(int opcode)
            {
                if (ret[0] != null)
                {
                    System.out.println("REWIRING METHOD EXIT TO CONTINUE TO THE ASPECT");
                    goTo(ret[0]);
                    ret[0] = null;
                }
                else
                {
                    super.onMethodExit(opcode);
                }
            }
        };
    }

    private Set<Aspect> gatherAspects(MethodModel mm)
    {
        List<AspectDiscoveryStrategy> discoveryStrategies = new ArrayList<AspectDiscoveryStrategy>();
        discoveryStrategies.add(new AnnotationAspectDiscoveryStrategy());

        Set<Aspect> aspects = new HashSet<Aspect>();
        for (AspectDiscoveryStrategy strategy : discoveryStrategies)
        {
            strategy.discover(mm, aspects);
        }
        return aspects;
    }

}
