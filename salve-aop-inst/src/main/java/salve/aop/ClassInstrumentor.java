package salve.aop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.Label;
import salve.asmlib.Method;
import salve.asmlib.MethodAdapter;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;
import salve.util.asm.AsmUtil;
import salve.util.asm.GeneratorAdapter;

class ClassInstrumentor extends ClassAdapter implements Opcodes
{
    private final AopAnalyzer analyzer;
    private String owner;
    private int uuid;

    public ClassInstrumentor(ClassVisitor cv, AopAnalyzer analyzer)
    {
        super(cv);
        this.analyzer = analyzer;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);
        this.owner = name;
    }

    protected String newAopDelegateMethodName(Method method)
    {
        return "_salve_aop" + uuid() + "$" + method.getName();
    }


    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc,
            final String signature, final String[] exceptions)
    {
        Method method = new Method(name, desc);
        List<Aspect> aspects = new ArrayList<Aspect>();
        for (Aspect aspect : analyzer.getAspects(method))
        {
            if (accept(aspect))
            {
                aspects.add(aspect);
            }
        }

        if (aspects.isEmpty() || analyzer.hasAnnotation(method, getMarkerAnnotationDesc()))
        {
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }

        String originName = name;
        String delegateName = name;
        MethodVisitor home = null;
        for (Aspect aspect : aspects)
        {
            delegateName = newAopDelegateMethodName(method);

            // create origin method which will call the delegate
            MethodVisitor originmv = cv
                    .visitMethod(access, originName, desc, signature, exceptions);
            home = (home == null) ? originmv : home;
            GeneratorAdapter origin = new GeneratorAdapter(originmv, access, name, desc);

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
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
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
            origin.visitLdcInsn(name);
            origin.loadLocal(types);
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            origin.visitVarInsn(ASTORE, methodVar);

            // MethodInvocation invocation = new ReflectiveInvocation(this, executor, method, <arg
            // valuess array>);
            final int invocation = origin.newLocal(Type
                    .getType("L/salve/aop/ReflectiveInvocation;"));
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
            origin.visitMethodInsn(INVOKESTATIC, aspect.clazz.replace(".", "/"), aspect.method,
                    "(Lsalve/aop/MethodInvocation;)Ljava/lang/Object;");

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
            if (exceptions != null)
            {
                for (String exception : exceptions)
                {
                    checkCastThrow(origin, ex, exception);
                }
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

            originName = delegateName;

        }

        MethodVisitor delegate = cv.visitMethod(access, delegateName, desc, signature, exceptions);
        delegate.visitAnnotation(getMarkerAnnotationDesc(), true);

        // mark entry method as processed
        home.visitAnnotation(getMarkerAnnotationDesc(), true);

        final MethodVisitor _home = home;
        return new MethodAdapter(delegate)
        {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible)
            {
                return _home.visitAnnotation(desc, visible);
            }
        };
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

    protected boolean accept(Aspect aspect)
    {
        return true;
    }

    protected String getMarkerAnnotationDesc()
    {
        return "Lsalve/aop/Instrumented;";
    }

    private static void pushInteger(MethodVisitor mv, int val)
    {
        if (val > 5)
        {
            mv.visitIntInsn(BIPUSH, val);
        }
        else
        {
            mv.visitInsn(ICONST_0 + val);
        }
    }

    protected int uuid()
    {
        return uuid++;
    }
}
