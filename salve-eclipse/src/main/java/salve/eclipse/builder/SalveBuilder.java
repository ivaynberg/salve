/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package salve.eclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import salve.BytecodeLoader;
import salve.CodeMarker;
import salve.CodeMarkerAware;
import salve.Config;
import salve.ConfigException;
import salve.InstrumentationContext;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.Scope;
import salve.asmlib.ClassReader;
import salve.config.XmlConfig;
import salve.config.XmlConfigReader;
import salve.eclipse.Activator;
import salve.eclipse.JavaProjectBytecodeLoader;
import salve.loader.BytecodePool;
import salve.loader.CompoundLoader;
import salve.loader.FilePathLoader;
import salve.model.ProjectModel;
import salve.monitor.NoopMonitor;
import salve.util.FallbackBytecodeClassLoader;
import salve.util.LruCache;
import salve.util.StreamsUtil;

public class SalveBuilder extends AbstractBuilder
{

    public static final String BUILDER_ID = "salve.eclipse.Builder";
    public static final String MARKER_ID = "salve.eclipse.marker.error.instrument";

    private LruCache<String, JarEntry> jarEntryCache = new LruCache<String, JarEntry>(100000);

    private final ReentrantReadWriteLock jarEntryCacheLock = new ReentrantReadWriteLock();
    private final JarEntry NOT_IN_JAR;

    private final Set<String> clearedMarkers = new HashSet<String>();

    private final ProjectModel model;

    public SalveBuilder()
    {
        super(MARKER_ID);
        NOT_IN_JAR = new JarEntry("string");
        model = new ProjectModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
    {

        long buildStart = System.currentTimeMillis();

        try
        {
            jarEntryCacheLock.writeLock().lock();
            jarEntryCache.resetStatistics();
        }
        finally
        {
            jarEntryCacheLock.writeLock().unlock();
        }

        removeMarks(getProject());
        clearedMarkers.clear();

        // find config resource
        final IResource configResource = findConfig();
        if (configResource == null)
        {
            markError(getProject(), "Could not find META-INF/salve.xml in any source folder");
            return null;
        }
        checkCancel(monitor);
        removeMarks(configResource);

        // load config
        BytecodeLoader bloader = new JavaProjectBytecodeLoader(getProject())
        {
            @Override
            protected BytecodeLoader newFilePathLoader(File file)
            {
                return new CachingFilePathLoader(file);
            }
        };
        ClassLoader cloader = new FallbackBytecodeClassLoader(getClass().getClassLoader(), bloader);

        final XmlConfig config = new XmlConfig();
        XmlConfigReader reader = new XmlConfigReader(cloader);
        try
        {
            reader.read(((IFile)configResource).getContents(), config);
        }
        catch (ConfigException e)
        {
            markError(configResource, "Could not configure Salve: " + e.getMessage());
            return null;
        }

        checkCancel(monitor);

        // check if we need to upgrade the build to full
        if (kind != FULL_BUILD)
        {
            final boolean[] modified = new boolean[] { false };
            getDelta(getProject()).accept(new IResourceDeltaVisitor()
            {

                public boolean visit(IResourceDelta delta) throws CoreException
                {
                    if (delta.getResource().equals(configResource))
                    {
                        switch (delta.getKind())
                        {
                            case IResourceDelta.ADDED :
                            case IResourceDelta.CHANGED :
                                modified[0] = true;
                                break;
                            case IResourceDelta.REMOVED :

                                break;

                        }
                        return false;
                    }
                    return true;
                }
            });

            if (modified[0])
            {
                // config file was modified, upgrade build to full
                kind = FULL_BUILD;
            }
        }

        // build

        // we use a bytecode pool instead of bloader directly because the pool
        // comes with a cache
        BytecodePool bytecodePool = new BytecodePool(Scope.ALL);
        bytecodePool.addLoader(bloader);

        ResourceBuilder builder = new ResourceBuilder(config, bytecodePool, monitor);
        if (kind == FULL_BUILD)
        {
            model.clear();
            getProject().accept(builder);
        }
        else
        {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null)
            {
                // TODO make sure it is ok to do this
                // getProject().accept(builder);
            }
            else
            {
                delta.accept(builder);
            }
        }

        long buildEnd = System.currentTimeMillis();
        long buildSeconds = (buildEnd - buildStart) / 1000;

        String info = String.format(
                "Salve build stats: jar cache: %dh/%dm bytecode cache: %dh/%dm build time: %ds",
                jarEntryCache.getHitCount(), jarEntryCache.getMissCount(), bytecodePool
                        .getCacheHitCount(), bytecodePool.getCacheMissCount(), buildSeconds);

        mark(getProject(), info, IMarker.SEVERITY_INFO);

        clearedMarkers.clear();

        return null;
    }

    private IJavaProject getJavaProject()
    {
        return JavaCore.create(getProject());
    }

    private IResource findConfig() throws CoreException
    {
        for (IClasspathEntry cpe : getJavaProject().getResolvedClasspath(true))
        {
            if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE)
            {
                IPath path = cpe.getPath();
                path = path.addTrailingSeparator().append("META-INF/salve.xml");
                IResource res = getProject().getWorkspace().getRoot().findMember(path);
                if (res != null && res.exists())
                {
                    return res;
                }
            }
        }
        return null;
    }

    class ResourceBuilder implements IResourceVisitor, IResourceDeltaVisitor
    {
        private final Config config;
        private final BytecodeLoader bloader;
        private final IProgressMonitor monitor;

        public ResourceBuilder(Config config, BytecodeLoader bloader, IProgressMonitor monitor)
        {
            super();
            this.config = config;
            this.bloader = bloader;
            this.monitor = monitor;
        }

        public boolean visit(IResource resource) throws CoreException
        {
            build(resource, config, bloader, monitor);
            return true;
        }

        public boolean visit(IResourceDelta delta) throws CoreException
        {
            switch (delta.getKind())
            {
                case IResourceDelta.ADDED :
                case IResourceDelta.CHANGED :
                    IResource resource = delta.getResource();
                    IMarker[] markers = resource.findMarkers(
                            "org.eclipse.core.resources.problemmarker", true, IResource.DEPTH_ONE);
                    boolean error = (markers != null && markers.length > 0);
                    if (!error)
                    {
                        build(resource, config, bloader, monitor);
                    }
                    break;
                case IResourceDelta.REMOVED :
                    break;
            }
            return true;
        }
    }

    public void build(IResource resource, Config config, BytecodeLoader bloader,
            IProgressMonitor monitor) throws CoreException
    {

        checkCancel(monitor);

        if (!(resource instanceof IFile) || !resource.getName().endsWith(".class"))
        {

            return;
        }

        final IFile file = (IFile)resource;

        final IResource source = findSourceResourceForClassResource(file);

        final String path = resource.getLocation().toOSString();
        if (!clearedMarkers.contains(path))
        {
            removeMarks(resource);
            clearedMarkers.add(path);
        }

        if (source != null)
        {
            final String srcPath = source.getLocation().toOSString();
            if (!clearedMarkers.contains(srcPath))
            {
                removeMarks(source);
                clearedMarkers.add(srcPath);
            }
        }
        try
        {


            final String cn = readClassName(file);

            for (Instrumentor inst : config.getInstrumentors(cn))
            {
                // System.out.println("instrumenting: " + cn + " with: "
                // + inst.getClass().getName());
                CompoundLoader cl = new CompoundLoader();
                cl.addLoader(new FileBytecodeLoader(file));
                cl.addLoader(bloader);

                model.setLoader(cl);
                InstrumentationContext ctx = new InstrumentationContext(cl, NoopMonitor.INSTANCE,
                        config.getScope(inst), model);

                byte[] bytecode = inst.instrument(cn, ctx);

                model.setLoader(null);
                file.setContents(new ByteArrayInputStream(bytecode), true, false, null);
            }
        }
        catch (InstrumentationException e)
        {
            log(IStatus.ERROR, "Error instrumenting: " + file.getName(), e);
            int lineNumber = -1;
            if (e instanceof CodeMarkerAware)
            {
                // TODO make sure file names in the marker and resource match
                final CodeMarker marker = ((CodeMarkerAware)e).getCodeMarker();
                if (marker != null)
                {
                    lineNumber = marker.getLineNumber();
                }
            }
            // e.printStackTrace();
            final IResource res = (source != null) ? source : file;
            markError(res, "Salve: " + e.getMessage(), lineNumber);
        }
    }

    private static IResource findSourceResourceForClassResource(IResource classResource)
    {
        IResource sourceResource = null;
        IJavaProject jp = JavaCore.create(classResource.getProject());
        int segs;
        try
        {
            segs = classResource.getProjectRelativePath().matchingFirstSegments(
                    jp.getOutputLocation().removeFirstSegments(1));

            if (segs > 0)
            {
                IPath res = classResource.getProjectRelativePath().removeFirstSegments(segs);

                // strip any inner class name from the top level class name
                String cn = res.segment(res.segmentCount() - 1);
                int end = cn.indexOf("$");
                if (end > 0)
                {
                    res = res.removeLastSegments(1);
                    res = res.append(cn.substring(0, end) + ".class");
                }

                IJavaElement el = jp.findElement(res);
                if (el != null)
                {
                    sourceResource = el.getCorrespondingResource();
                }
            }
        }
        catch (JavaModelException e)
        {
            // noop
        }
        return sourceResource;

    }

    private String readClassName(final IFile file) throws CoreException
    {
        ClassReader reader;
        final InputStream is = file.getContents();
        try
        {
            reader = new ClassReader(is);
            final String cn = reader.getClassName();
            return cn;
        }
        catch (IOException e)
        {
            // log(IStatus.ERROR, "error reading: "
            // + file.getFullPath().toOSString(), e);
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Unable to parse class file: " + file.getFullPath().toOSString(), e);
            throw new CoreException(status);
        }
        finally
        {
            StreamsUtil.close(is, "Could not close input stream for file {}", file.getFullPath()
                    .toOSString());
        }

    }

    private class CachingFilePathLoader extends FilePathLoader
    {

        public CachingFilePathLoader(File path)
        {
            super(path);
        }

        @Override
        protected JarEntry findJarEntry(File jar, String name) throws IOException
        {

            final String cacheKey = jar.lastModified() + jar.getAbsolutePath() + name;
            jarEntryCacheLock.readLock().lock();
            try
            {
                JarEntry cached = jarEntryCache.get(cacheKey);
                if (cached != null)
                {
                    if (cached == NOT_IN_JAR)
                    {
                        return null;
                    }
                    else
                    {
                        return cached;
                    }
                }
            }
            finally
            {
                jarEntryCacheLock.readLock().unlock();
            }

            // XXX instead of releasing read lock and acquiring write lock we
            // should upgrade the read lock to write lock

            JarEntry entry = super.findJarEntry(jar, name);

            jarEntryCacheLock.writeLock().lock();
            try
            {
                jarEntryCache.put(cacheKey, (entry == null) ? NOT_IN_JAR : entry);
            }
            finally
            {
                jarEntryCacheLock.writeLock().unlock();
            }
            return entry;

        }
    }
}
