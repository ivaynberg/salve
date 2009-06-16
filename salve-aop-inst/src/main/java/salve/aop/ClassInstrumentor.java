package salve.aop;

import java.util.Collection;

import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.Label;
import salve.asmlib.Method;
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

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc,
            final String signature, final String[] exceptions)
    {
        Collection<Aspect> aspects = analyzer.getAspects(new Method(name, desc));

        if (aspects.isEmpty())
        {
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }

        MethodVisitor delegate = null;


        for (Aspect aspect : aspects)
        {

            final String delegateName = "_salve_aop$" + name + uuid();

            // create origin method which will call the delegate
            MethodVisitor originmv = cv.visitMethod(access, name, desc, signature, exceptions);
            GeneratorAdapter origin = new GeneratorAdapter(originmv, access, name, desc);

            origin.visitCode();

            final Label start = new Label();
            final Label end = new Label();
            final Label securityException = new Label();
            final Label noSuchMethodException = new Label();
            final Label endSecurityException = new Label();
            final Label endNoSuchMethodException = new Label();
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
            final int method = origin.newLocal(Type.getType("Ljava/lang/reflect/Method;"));
            origin.visitVarInsn(ALOAD, clazz);
            origin.visitLdcInsn(name);
            origin.loadLocal(types);
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            origin.visitVarInsn(ASTORE, method);

            // MethodInvocation invocation = new ReflectiveInvocation(this, executor, method, <arg
            // valuess array>);
            final int invocation = origin.newLocal(Type
                    .getType("L/salve/aop/ReflectiveInvocation;"));
            origin.visitTypeInsn(NEW, "salve/aop/ReflectiveInvocation");
            origin.visitInsn(DUP);
            origin.visitVarInsn(ALOAD, 0);
            origin.visitVarInsn(ALOAD, executor);
            origin.visitVarInsn(ALOAD, method);
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
            final Type ret = new Method(name, desc).getReturnType();
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

            delegate = cv.visitMethod(access, delegateName, desc, signature, exceptions);
        }


        return delegate;
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
