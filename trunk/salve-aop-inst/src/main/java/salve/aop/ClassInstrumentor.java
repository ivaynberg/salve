package salve.aop;

import java.util.Collection;

import salve.asmlib.ClassAdapter;
import salve.asmlib.ClassVisitor;
import salve.asmlib.Label;
import salve.asmlib.LocalVariablesSorter;
import salve.asmlib.Method;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Opcodes;
import salve.asmlib.Type;
import salve.util.asm.AsmUtil;

class ClassInstrumentor extends ClassAdapter implements Opcodes
{
    private final AopAnalyzer analyzer;
    private String owner;

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
        Method m = new Method(name, desc);
        Collection<Aspect> aspects = analyzer.getAspects(m);

        if (aspects.isEmpty())
        {
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }

        MethodVisitor delegate = null;

        int index = 0;
        for (Aspect aspect : aspects)
        {

            final String delegateName = "_salve_aop$" + name + index;

            // create origin method which will call the delegate
            MethodVisitor originmv = cv.visitMethod(access, name, desc, signature, exceptions);
            LocalVariablesSorter origin = new LocalVariablesSorter(access, desc, originmv);

            origin.visitCode();

            final Label start = new Label();
            final Label end = new Label();
            final Label securityException = new Label();
            final Label noSuchMethodException = new Label();
            final Label endSecurityException = new Label();
            final Label endNoSuchMethodException = new Label();

            origin.visitTryCatchBlock(start, end, securityException, "java/lang/SecurityException");
            origin.visitTryCatchBlock(start, end, noSuchMethodException,
                    "java/lang/NoSuchMethodException");

            origin.visitLabel(start);

            // Object[] args=new Object[<arg count>];
            final int args = origin.newLocal(Type.getType("[Ljava/lang/Object;"));
            pushInteger(origin, m.getArgumentTypes().length);
            origin.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            origin.visitVarInsn(ASTORE, args);

            // args[0]=arg1; args[1]=arg2; ...
            int idx = 1;
            for (Type type : m.getArgumentTypes())
            {
                if (AsmUtil.isPrimitive(type))
                {

                }
                else
                {

                }
            }

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
            origin.visitInsn(ACONST_NULL);
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            origin.visitVarInsn(ASTORE, executor);

            // final Method method = clazz.getDeclaredMethod("hello", null);
            final int method = origin.newLocal(Type.getType("Ljava/lang/reflect/Method;"));
            origin.visitVarInsn(ALOAD, 1);
            origin.visitLdcInsn("hello");
            origin.visitInsn(ACONST_NULL);
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethod",
                    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            origin.visitVarInsn(ASTORE, method);

            // final MethodInvocation invocation = new ReflectiveInvocation(this, executor, method,
            // null);
            final int invocation = origin.newLocal(Type
                    .getType("L/salve/aop/ReflectiveInvocation;"));
            origin.visitTypeInsn(NEW, "salve/aop/ReflectiveInvocation");
            origin.visitInsn(DUP);
            origin.visitVarInsn(ALOAD, 0);
            origin.visitVarInsn(ALOAD, executor);
            origin.visitVarInsn(ALOAD, method);
            origin.visitInsn(ACONST_NULL);
            origin
                    .visitMethodInsn(INVOKESPECIAL, "salve/aop/ReflectiveInvocation", "<init>",
                            "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)V");
            origin.visitVarInsn(ASTORE, invocation);

            // TracedAdvice.advise(invocation);
            origin.visitVarInsn(ALOAD, invocation);
            origin.visitMethodInsn(INVOKESTATIC, aspect.clazz.replace(".", "/"), aspect.method,
                    "(Lsalve/aop/MethodInvocation;)Ljava/lang/Object;");
            origin.visitInsn(POP);

            origin.visitInsn(RETURN);
            origin.visitLabel(end);

            // catch SecurityExcepton
            origin.visitLabel(securityException);
            origin.visitVarInsn(ASTORE, 1); // XXX we are reusing the local var slot, hope thats ok
            origin.visitVarInsn(ALOAD, 1);
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/SecurityException", "printStackTrace",
                    "()V");
            origin.visitLabel(endSecurityException);
            origin.visitInsn(RETURN);

            // catch NoSuchMethodException
            origin.visitLabel(noSuchMethodException);
            origin.visitVarInsn(ASTORE, 1); // XXX we are reusing the local var slot, hope thats ok
            origin.visitVarInsn(ALOAD, 1);
            origin.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NoSuchMethodException",
                    "printStackTrace", "()V");
            origin.visitLabel(endNoSuchMethodException);
            origin.visitInsn(RETURN);

            origin.visitMaxs(0, 0);
            origin.visitEnd();

            delegate = cv.visitMethod(access, delegateName, desc, signature, exceptions);

            index++;
        }


        return delegate;
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
}
