package salve.eclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import salve.BytecodeLoader;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.config.Config;
import salve.config.ConfigException;
import salve.config.PackageConfig;
import salve.config.XmlConfigReader;
import salve.eclipse.Activator;
import salve.eclipse.JavaProjectBytecodeLoader;
import salve.loader.CompoundLoader;
import salve.util.FallbackBytecodeClassLoader;

public class SalveBuilder extends AbstractBuilder {

	public static final String BUILDER_ID = "salve.eclipse.Builder";
	private static final String MARKER_ID = "salve.eclipse.marker.error.instrument";

	public SalveBuilder() {
		super(MARKER_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		removeMarks(getProject());
		BytecodeLoader bloader = new JavaProjectBytecodeLoader(getProject());

		// find config resource
		final IResource configResource = findConfig();
		if (configResource == null) {
			markError(getProject(),
					"Could not find META-INF/salve.xml in any source folder");
			return null;
		}

		removeMarks(configResource);

		// load config
		ClassLoader cloader = new FallbackBytecodeClassLoader(getClass()
				.getClassLoader(), bloader);
		Config config = null;
		try {
			config = loadConfig(configResource, cloader);
		} catch (ConfigException e) {
			markError(configResource, "Could not configure Salve: "
					+ e.getMessage());
			return null;
		}

		// check if we need to upgrade the build to full
		if (kind != FULL_BUILD) {
			switch (findResourceDeltaKind(configResource)) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				// config file was modified, upgrade build to full
				kind = FULL_BUILD;
				break;
			}
		}

		// build
		ResourceBuilder builder = new ResourceBuilder(config, bloader);
		if (kind == FULL_BUILD) {
			getProject().accept(builder);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				getProject().accept(builder);
			} else {
				delta.accept(builder);
			}
		}

		return null;
	}

	private void build(IResource resource, Config config, BytecodeLoader bloader)
			throws CoreException {
		if (!(resource instanceof IFile)
				|| !resource.getName().endsWith(".class")) {
			return;
		}

		final IFile file = (IFile) resource;

		removeMarks(resource);
		try {
			ClassReader reader = new ClassReader(file.getContents());
			final String cn = reader.getClassName();
			PackageConfig conf = config.getPackageConfig(cn.replace("/", "."));
			if (conf != null) {
				for (Instrumentor inst : conf.getInstrumentors()) {
					System.out.println("instrumenting: " + cn + " with: "
							+ inst.getClass().getName());
					CompoundLoader cl = new CompoundLoader();
					cl.addLoader(new FileBytecodeLoader(file));
					cl.addLoader(bloader);
					byte[] bytecode = inst.instrument(cn, cl);
					file.setContents(new ByteArrayInputStream(bytecode), true,
							false, null);
				}
			}
		} catch (InstrumentationException e) {
			markError(resource, "Instrumentation error: " + e.getMessage());
		} catch (IOException e) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Unable to parse class file: " + file.getName(), e);
			throw new CoreException(status);
		}

	}

	private IResource findConfig() throws CoreException {
		for (IClasspathEntry cpe : getJavaProject().getResolvedClasspath(true)) {
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPath path = cpe.getPath();
				path = path.addTrailingSeparator().append("META-INF/salve.xml");
				IResource res = getProject().getWorkspace().getRoot()
						.findMember(path);
				if (res != null && res.exists()) {
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * @param configResource
	 * @param modified
	 * @throws CoreException
	 */
	private int findResourceDeltaKind(final IResource configResource)
			throws CoreException {

		final int[] kind = new int[] { 0 };
		getDelta(getProject()).accept(new IResourceDeltaVisitor() {

			public boolean visit(IResourceDelta delta) throws CoreException {
				if (delta.getResource().equals(configResource)) {
					kind[0] = delta.getKind();
					return false;
				} else {
					return true;
				}
			}
		});
		return kind[0];
	}

	private IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

	/**
	 * @param configResource
	 * @param cloader
	 * @throws ConfigException
	 * @throws CoreException
	 */
	private Config loadConfig(final IResource configResource,
			ClassLoader cloader) throws ConfigException, CoreException {
		final Config config = new Config();
		XmlConfigReader reader = new XmlConfigReader(cloader);
		reader.read(((IFile) configResource).getContents(), config);
		return config;
	}

	class ResourceBuilder implements IResourceVisitor, IResourceDeltaVisitor {
		private final Config config;
		private final BytecodeLoader bloader;

		public ResourceBuilder(Config config, BytecodeLoader bloader) {
			super();
			this.config = config;
			this.bloader = bloader;
		}

		public boolean visit(IResource resource) throws CoreException {
			build(resource, config, bloader);
			return true;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				build(delta.getResource(), config, bloader);
				break;
			case IResourceDelta.REMOVED:
				break;
			}
			return true;
		}
	}
}
