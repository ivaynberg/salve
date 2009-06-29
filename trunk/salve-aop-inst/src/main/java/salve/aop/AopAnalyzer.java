package salve.aop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import salve.BytecodeLoader;
import salve.CannotLoadBytecodeException;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassReader;
import salve.asmlib.MethodVisitor;
import salve.asmlib.Type;
import salve.util.asm.AnnotationVisitorAdapter;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.MethodVisitorAdapter;

public class AopAnalyzer extends ClassVisitorAdapter
{
    public static final OverrideInfo EMPTY = new OverrideInfo();

    private static final int META_VISITOR = ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES |
            ClassReader.SKIP_DEBUG;

    private final Map<Method, Collection<Aspect>> meta;
    private final Map<Method, Collection<String>> annots;
    private final Map<Method, OverrideInfo> methods = new HashMap<Method, OverrideInfo>();
    private final BytecodeLoader loader;

    public AopAnalyzer(BytecodeLoader loader)
    {
        meta = new HashMap<Method, Collection<Aspect>>();
        annots = new HashMap<Method, Collection<String>>();
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
                final Method method = new Method(access, name, desc);
                methods.put(method, EMPTY);
                return new MethodVisitorAdapter()
                {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
                    {
                        recordMethodAnnotation(method, desc);
                        analyzeAnnotation(method, desc);
                        return super.visitAnnotation(desc, visible);
                    }

                    @Override
                    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
                            boolean visible)
                    {
                        analyzeAnnotation(method, desc);
                        return super.visitParameterAnnotation(parameter, desc, visible);
                    }

                };
            }
        }, META_VISITOR);

        if (reader.getSuperName() != null)
        {
            analyzeSuper(reader.getSuperName());
        }

    }

    private void analyzeSuper(String name)
    {
        if (name.startsWith("java/"))
        {
            return;
        }

        byte[] bytecode = loader.loadBytecode(name);
        if (bytecode == null)
        {
            throw new CannotLoadBytecodeException(name);
        }
        ClassReader reader = new ClassReader(bytecode);

        reader.accept(new ClassVisitorAdapter()
        {
            String owner = null;
            Method override = null;

            @Override
            public void visit(int version, int access, String name, String signature,
                    String superName, String[] interfaces)
            {
                owner = name;
// System.out.println("analyzing super: "+owner);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                    String signature, String[] exceptions)
            {
//                System.out.println("analyzer visiting: " + owner + "#" + name + " " + desc);


                Method m = new Method(access, name, desc);

                for (Method submethod : methods.keySet())
                {
                    if (submethod.canOverride(m))
                    {
                        override = submethod;
                        break;
                    }
                }

                if (name.equals("<init>"))
                {
                    // FIXME skip constructors for now
                    override = null;
                }


                if (override == null)
                {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }

                final Method method = override;

                return new MethodVisitorAdapter()
                {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
                    {
                        if (desc.equals("Lsalve/aop/Instrumented;"))
                        {
                            return new AnnotationVisitorAdapter()
                            {
                                String root = null;

                                @Override
                                public void visit(String name, Object value)
                                {
                                    if ("root".equals(name))
                                    {
                                        root = (String)value;
                                    }
                                }

                                @Override
                                public void visitEnd()
                                {
                                    if (root == null)
                                    {
                                        throw new IllegalStateException();
                                    }

                                    if (override != null)
                                    {
                                        OverrideInfo info = methods.get(override);
                                        if (info == null || info == EMPTY)
                                        {
                                            // if override info is set then we do not want super
                                            // super to override super
                                            info = new OverrideInfo(owner, method.getName(), method
                                                    .getDesc(), root);

                                            methods.put(override, info);
                                        }
                                    }


                                    super.visitEnd();
                                }
                            };
                        }
                        recordMethodAnnotation(method, desc);
                        analyzeAnnotation(method, desc, true);
                        return super.visitAnnotation(desc, visible);
                    }

                    @Override
                    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
                            boolean visible)
                    {
                        analyzeAnnotation(method, desc, true);
                        return super.visitParameterAnnotation(parameter, desc, visible);
                    }

                };
            }
        }, META_VISITOR);

        if (reader.getSuperName() != null)
        {
            analyzeSuper(reader.getSuperName());
        }

    }

    public boolean hasAnnotation(Method method, String desc)
    {
        Collection<String> collection = annots.get(method);
        if (collection == null)
        {
            return false;
        }
        else
        {
            return collection.contains(desc);
        }
    }


    private void recordMethodAnnotation(Method method, String desc)
    {
        Collection<String> collection = annots.get(method);
        if (collection == null)
        {
            collection = new ArrayList<String>(1);
        }
        collection.add(desc);
        annots.put(method, collection);
    }

    private void analyzeAnnotation(final Method method, String annotDesc)
    {
        analyzeAnnotation(method, annotDesc, false);
    }

    private void analyzeAnnotation(final Method method, String annotDesc, boolean inheritedOnly)
    {
        final String name = Type.getType(annotDesc).getInternalName();
        byte[] bytecode = loader.loadBytecode(name);
        if (bytecode == null)
        {
            throw new CannotLoadBytecodeException(name);
        }
        ClassReader reader = new ClassReader(bytecode);

        final List<Aspect> aspects = new ArrayList<Aspect>(0);
        final boolean[] inherited = new boolean[1];

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
                            aspects.add(aspect);
                        }
                    };
                }
                else
                {
                    if (desc.equals("Ljava/lang/annotation/Inherited;"))
                    {
                        inherited[0] = true;
                    }
                    return super.visitAnnotation(desc, visible);
                }
            }
        }, META_VISITOR);

        if (!inheritedOnly || (inheritedOnly && inherited[0] == true))
        {
            for (Aspect aspect : aspects)
            {
                addAspect(method, aspect);
            }
        }
    }


    private void addAspect(Method method, Aspect aspect)
    {
        Collection<Aspect> aspects = meta.get(method);
        if (aspects == null)
        {
            aspects = new ArrayList<Aspect>(1);
        }
        if (!aspects.contains(aspect))
        {
            aspects.add(aspect);
        }
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

    public OverrideInfo getOverrideInfo(Method method)
    {
        OverrideInfo info = methods.get(method);
        if (info == null || info == EMPTY)
        {
            return null;
        }
        else
        {
            return info;
        }
    }
}
