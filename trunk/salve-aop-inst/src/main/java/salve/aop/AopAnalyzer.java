package salve.aop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassReader;
import salve.asmlib.Method;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.util.asm.AnnotationVisitorAdapter;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.MethodVisitorAdapter;

public class AopAnalyzer extends ClassVisitorAdapter
{
    private static final int META_VISITOR = ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES |
            ClassReader.SKIP_DEBUG;

    private final Map<Method, Collection<Aspect>> meta;
    private final BytecodeLoader loader;

    public AopAnalyzer(BytecodeLoader loader)
    {
        meta = new HashMap<Method, Collection<Aspect>>();
        this.loader = loader;
    }

    public void analyze(byte[] bytecode)
    {
        ClassReader reader = new ClassReader(bytecode);

        reader.accept(new ClassVisitorAdapter()
        {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                    String signature, String[] exceptions)
            {
                final Method method = new Method(name, desc);
                return new MethodVisitorAdapter()
                {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
                    {
                        final String name = Type.getType(desc).getInternalName();
                        byte[] bytecode = loader.loadBytecode(name);
                        if (bytecode == null)
                        {
                            throw new CannotLoadBytecodeException(name);
                        }
                        ClassReader reader = new ClassReader(bytecode);
                        reader.accept(new ClassVisitorAdapter()
                        {
                            @Override
                            public AnnotationVisitor visitAnnotation(String desc, boolean visible)
                            {
                                if (desc.equals("Lsalve/aop/MethodAdvice;"))
                                {
                                    return new AnnotationVisitorAdapter()
                                    {
                                        private final Aspect aspect = new Aspect();

                                        @Override
                                        public void visit(String name, Object value)
                                        {
                                            if (name.equals("instrumentorClass"))
                                            {
                                                aspect.clazz = value.toString();
                                            }
                                            else if (name.equals("instrumentorMethod"))
                                            {
                                                aspect.method = value.toString();
                                            }
                                        }

                                        @Override
                                        public void visitEnd()
                                        {
                                            addAspect(method, aspect);
                                        }
                                    };
                                }
                                else
                                {
                                    return super.visitAnnotation(desc, visible);
                                }
                            }
                        }, META_VISITOR);
                        return super.visitAnnotation(desc, visible);
                    }
                };
            }
        }, META_VISITOR);


    }

    private void addAspect(Method method, Aspect aspect)
    {
        Collection<Aspect> aspects = meta.get(method);
        if (aspects == null)
        {
            aspects = new ArrayList<Aspect>(1);
        }
        aspects.add(aspect);
        meta.put(method, aspects);
    }

    public Collection<Aspect> getAspects(Method method)
    {
        Collection<Aspect> aspects = meta.get(method);
        if (aspects == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableCollection(aspects);
        }
    }
}
