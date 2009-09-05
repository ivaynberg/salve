package salve.aop.inst;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import salve.InstrumentationContext;
import salve.InstrumentorMonitor;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.Label;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;
import salve.model.AnnotationModel;
import salve.model.MethodModel;
import salve.model.ProjectModel;
import salve.util.asm.AsmUtil;
import salve.util.asm.GeneratorAdapter;

class ClassInstrumentor extends ClassAdapter implements Opcodes
{
    private String owner;
    private final ProjectModel model;
    private final Set< ? extends AspectDiscoveryStrategy> discoveryStrategies;
    private final InstrumentorMonitor monitor;

    public ClassInstrumentor(ClassVisitor cv,
            Set< ? extends AspectDiscoveryStrategy> discoveryStrategies, InstrumentationContext ctx)
    {
        super(cv);
        this.discoveryStrategies = discoveryStrategies;
        this.monitor = ctx.getMonitor();
        this.model = ctx.getModel();
    }

    private Set<Aspect> gatherAspects(MethodModel mm)
    {
        Set<Aspect> aspects = new HashSet<Aspect>();
        for (AspectDiscoveryStrategy strategy : discoveryStrategies)
        {
            strategy.discover(mm, aspects);
        }
        return aspects;
    }


    protected ProjectModel getModel()
    {
        return model;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);
        this.owner = name;
    }

    private String newDelegateMethodName(MethodModel method, Aspect aspect)
    {
        return method.getName() + "$aop$" + aspect.getMethod() + "_" + uuid();
    }


    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc,
            final String signature, final String[] exceptions)
    {
        MethodModel method = model.getClass(owner).getMethod(name, desc);

        final Set<Aspect> aspects = gatherAspects(method);

        AnnotationModel marker = method.getAnnot(getInstrumentationMarkerAnnotationDesc());

        if (aspects.isEmpty() || marker != null)
        {
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }

        String wrapperName = name;

        // origin->delegate->delegate->root
        // orogin - original method
        // delegate - delegate methods generated as part of call chain
        // root - final delegate containing original method's code

        MethodVisitor origin = null;
        for (Aspect aspect : aspects)
        {
            int wrapperAccess = ACC_PRIVATE ;
            if (origin == null)
            {
                wrapperAccess = access;
            }
            
            // create aspect wrapper method which will call the delegate
            MethodVisitor wrapper = cv.visitMethod(wrapperAccess, wrapperName, desc, signature,
                    exceptions);

            // remember the origin method
            if (origin == null)
            {
                origin = wrapper;
            }

            wrapperName = generateWrapperMethod(method, wrapper, aspect);

        }
        final String rootName = wrapperName;

        // TODO factor out into a method
        // add @Instrumented(root=<root method name>, aspects=<...>) to the origin method
        AnnotationVisitor visitor = origin.visitAnnotation(
                getInstrumentationMarkerAnnotationDesc(), true);
        visitor.visit("root", rootName);
// StringBuilder _aspects = new StringBuilder();
// for (Aspect aspect : aspects)
// {
// if (_aspects.length() > 0)
// {
// _aspects.append(",");
// }
// _aspects.append(aspect.encode());
// }
// visitor.visit("aspects", _aspects.toString());
        visitor.visitEnd();


        // generate the root method

        // if super of this method was aop-instrumented we need to reroute super calls to the root
        // of the super method so that they skip the aspect chain. inherited aspects would have been
        // applied directly to this method.

        // we also forward any annotations from the root method to the origin method

        final OverrideInfo info = getOverrideInfo(method);

        MethodVisitor root = cv.visitMethod(ACC_PROTECTED, rootName, desc, signature,
                exceptions);

        final MethodVisitor _origin = origin;
        return new MethodAdapter(root)
        {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible)
            {
                // ignore previous instrumentation markers
                if (desc.equals(getInstrumentationMarkerAnnotationDesc()))
                {
                    return null;
                }
                // forward annots to the root to the origin

                for (Aspect aspect : aspects)
                {
                    final AnnotationProcessor processor = aspect.getAnnotationProcessor();
                    if (processor != null && processor.filter(desc))
                    {
                        return processor.filter(_origin, desc, visible);
                    }
                }

                return _origin.visitAnnotation(desc, visible);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc)
            {
                if (opcode == Opcodes.INVOKESPECIAL)
                {
                    // rewrite super calls
                    if (info != null && info.getType().equals(owner) &&
                            info.getMethod().equals(name) && desc.equals(info.getDesc()))
                    {
                        super.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, info
                                .getRootDelegateMethodName(), desc);
                        return;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        };
    }

    private OverrideInfo getOverrideInfo(MethodModel method)
    {
        if (method.getSuper() != null)
        {
            MethodModel sup = method.getSuper();
            AnnotationModel annot = sup.getAnnot("Lsalve/aop/Instrumented;");
            if (annot != null)
            {
                return new OverrideInfo(sup.getClassModel().getName(), sup.getName(),
                        sup.getDesc(), annot.getField("root").getValue().toString());
            }
        }
        return null;
    }

    private String generateWrapperMethod(MethodModel method, MethodVisitor originmv, Aspect aspect)
    {
        final String delegateName = newDelegateMethodName(method, aspect);
        GeneratorAdapter origin = new GeneratorAdapter(originmv, method.getAccess(), method
                .getName(), method.getDesc());

        monitor.methodModified(owner, method.getAccess(), method.getName(), method.getDesc());


        origin.visitCode();

        final Label start = new Label();
        final Label end = new Label();
        final Label securityException = new Label();
        final Label noSuchMethodException = new Label();
        final Label invocationStart = new Label();
        final Label invocationEnd = new Label();
        final Label invocationException = new Label();

        origin.visitTryCatchBlock(start, end, securityException, "java/lang/SecurityException");
        origin.visitTryCatchBlock(start, end, noSuchMethodException,
                "java/lang/NoSuchMethodException");
        origin.visitTryCatchBlock(invocationStart, invocationEnd, invocationException,
                "java/lang/Throwable");
        origin.visitLabel(start);

        final int ex = origin.newLocal(Type.getType("Ljava/lang/Throwable;"));

        // Object[] args=new Object[<arg count>];
        final int args = origin.newLocal(Type.getType("Ljava/lang/Object;"));
        origin.loadArgArray();
        origin.storeLocal(args);

        // Object[] argTypes=<array of argument types>
        final int types = origin.newLocal(Type.getType("Ljava/lang/Class;"));
        origin.loadArgTypesArray();
        origin.storeLocal(types);

        // final Class<?> clazz=getClass();
        final int clazz = origin.newLocal(Type.getType("Ljava/lang/Object;"));
        origin.visitVarInsn(ALOAD, 0);
        origin
                .visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
                        "()Ljava/lang/Class;");
        origin.visitVarInsn(ASTORE, clazz);

        // final Method executor = clazz.getDeclaredMethod("_salve_aop$hello", null);
        final int executor = origin.newLocal(Type.getType("Ljava/lang/reflect/Method;"));
        origin.visitVarInsn(ALOAD, clazz);
        origin.visitLdcInsn(delegateName);
        origin.loadLocal(types);
        origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
        origin.visitVarInsn(ASTORE, executor);

        // final Method method = clazz.getDeclaredMethod("hello", <arg types array>);
        final int methodVar = origin.newLocal(Type.getType("Ljava/lang/reflect/Method;"));
        origin.visitVarInsn(ALOAD, clazz);
        origin.visitLdcInsn(method.getName());
        origin.loadLocal(types);
        origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
        origin.visitVarInsn(ASTORE, methodVar);

        // MethodInvocation invocation = new ReflectiveInvocation(this, executor, method, <arg
        // valuess array>);
        final int invocation = origin.newLocal(Type.getType("L/salve/aop/ReflectiveInvocation;"));
        origin.visitTypeInsn(NEW, "salve/aop/ReflectiveInvocation");
        origin.visitInsn(DUP);
        origin.visitVarInsn(ALOAD, 0);
        origin.visitVarInsn(ALOAD, executor);
        origin.visitVarInsn(ALOAD, methodVar);
        origin.loadLocal(args);
        origin
                .visitMethodInsn(INVOKESPECIAL, "salve/aop/ReflectiveInvocation", "<init>",
                        "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)V");
        origin.visitVarInsn(ASTORE, invocation);


        origin.mark(invocationStart);
        // return <advice class>.<advice method>(invocation);
        origin.visitVarInsn(ALOAD, invocation);
        origin.visitMethodInsn(INVOKESTATIC, aspect.getClazz().replace(".", "/"), aspect
                .getMethod(), "(Lsalve/aop/MethodInvocation;)Ljava/lang/Object;");

        // return value returned by the invocation
        final Type ret = method.getReturnType();
        if (ret.equals(Type.VOID_TYPE))
        {
            // void method, pop aspect's return value off the stack
            // TODO possible check to make sure an aspect always returns null for void methods
            origin.visitInsn(POP);
        }
        else
        {
            if (AsmUtil.isPrimitive(ret))
            {
                // primitivereturn value, need to unbox before returning
                origin.unbox(ret);
            }
            else
            {
                // non-primitive return value, cast and return
                origin.checkCast(ret);
            }
            origin.returnValue();
        }


        origin.mark(invocationEnd);

        origin.visitInsn(RETURN);
        origin.visitLabel(end);

        // catch (SecurityExcepton ex) throw new AspectInvocationException(ex);
        origin.visitLabel(securityException);
        origin.visitVarInsn(ASTORE, ex);
        origin.visitTypeInsn(NEW, "salve/aop/AspectInvocationException");
        origin.visitInsn(DUP);
        origin.visitVarInsn(ALOAD, ex);
        origin.visitMethodInsn(INVOKESPECIAL, "salve/aop/AspectInvocationException", "<init>",
                "(Ljava/lang/Throwable;)V");
        origin.visitInsn(ATHROW);

        // catch (NoSuchMethodException ex) throw new AspectInvocationException(ex);
        origin.visitLabel(noSuchMethodException);
        origin.visitVarInsn(ASTORE, ex);
        origin.visitTypeInsn(NEW, "salve/aop/AspectInvocationException");
        origin.visitInsn(DUP);
        origin.visitVarInsn(ALOAD, ex);
        origin.visitMethodInsn(INVOKESPECIAL, "salve/aop/AspectInvocationException", "<init>",
                "(Ljava/lang/Throwable;)V");
        origin.visitInsn(ATHROW);

        origin.visitLabel(invocationException);

        // catch Throwable t <-- from aspect invocation
        origin.visitVarInsn(ASTORE, ex);

        // check if t is an InvocationTargetException and unwrap it before we process it
        origin.visitVarInsn(ALOAD, ex);
        origin.visitTypeInsn(INSTANCEOF, "java/lang/reflect/InvocationTargetException");
        Label doneUnwrapping = new Label();
        origin.visitJumpInsn(IFEQ, doneUnwrapping);
        origin.visitVarInsn(ALOAD, ex);
        origin.visitTypeInsn(CHECKCAST, "java/lang/reflect/InvocationTargetException");
        origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/InvocationTargetException",
                "getCause", "()Ljava/lang/Throwable;");
        origin.visitVarInsn(ASTORE, ex);
        origin.visitLabel(doneUnwrapping);


        // if (t instanceof RuntimeException) throw t;
        checkCastThrow(origin, ex, "java/lang/RuntimeException");


        // if (t instanceof <any in throw decl>) throw t;
        for (String exception : method.getExceptions())
        {
            checkCastThrow(origin, ex, exception);
        }

        // unknown non-runtime ex, wrap in runtime ex and throw
        origin.visitTypeInsn(NEW, "salve/aop/UndeclaredException");
        origin.visitInsn(DUP);
        origin.visitVarInsn(ALOAD, ex);
        origin.visitMethodInsn(INVOKESPECIAL, "salve/aop/UndeclaredException", "<init>",
                "(Ljava/lang/Throwable;)V");
        origin.visitInsn(ATHROW);

        origin.visitMaxs(0, 0);
        origin.visitEnd();

        return delegateName;
    }

    private static void checkCastThrow(MethodVisitor mv, int local, String type)
    {
        Label after = new Label();

        mv.visitVarInsn(ALOAD, local);
        mv.visitTypeInsn(INSTANCEOF, type);
        mv.visitJumpInsn(IFEQ, after);
        mv.visitVarInsn(ALOAD, local);
        mv.visitTypeInsn(CHECKCAST, type);
        mv.visitInsn(ATHROW);
        mv.visitLabel(after);
    }

    private String getInstrumentationMarkerAnnotationDesc()
    {
        return "Lsalve/aop/Instrumented;";
    }

    private String uuid()
    {
        return UUID.randomUUID().toString().replaceAll("[^a-z0-9]", "");
    }

}