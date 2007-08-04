package salve.eclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
	private Config config;
	private BytecodeLoader loader;

	public SalveBuilder() {
		super(MARKER_ID);
	}

	@Override
	protected void onBuild(IResource resource) throws CoreException {
		if (!(resource instanceof IFile)) {
			return;
		}

		final IFile file = (IFile) resource;

		/*
		 * if (file.getName().endsWith("/META-INF/salve.xml")) { if (config ==
		 * null) { try { config = readConfig(file); } catch (ConfigException e) {
		 * config=null; } return; } if (config!=null) { // if salve.xml was
		 * changed we need to rebuild the entire // project // upgrade this
		 * incremental build to full build // TODO we need a way to break out of
		 * this build completely if (getBuildKind() != FULL_BUILD) {
		 * build(FULL_BUILD, getBuildArgs(), getBuildMonitor()); } } }
		 */

		if (config != null && (resource instanceof IFile)
				&& resource.getName().endsWith(".class")) {

			removeMarks(resource);
			try {

				ClassReader reader;
				reader = new ClassReader(file.getContents());
				final String cn = reader.getClassName();
				PackageConfig conf = config.getPackageConfig(cn.replace("/",
						"."));
				if (conf != null) {
					for (Instrumentor inst : conf.getInstrumentors()) {
						System.out.println("instrumenting: " + cn + " with: "
								+ inst.getClass().getName());
						CompoundLoader cl = new CompoundLoader();
						cl.addLoader(new FileBytecodeLoader(file));
						cl.addLoader(this.loader);
						byte[] bytecode = inst.instrument(cn, cl);
						file.setContents(new ByteArrayInputStream(bytecode),
								true, false, null);
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
	}

	@Override
	protected void onEndBuild() {
	}

	@Override
	protected void onStartBuild() throws CoreException {
		removeMarks(getProject());
		loader = new JavaProjectBytecodeLoader(getProject());
		initConfig();
	}

	private Config readConfig(IFile file) throws CoreException, ConfigException {
		try {
			removeMarks(file);
			InputStream is = file.getContents(false);
			XmlConfigReader reader = new XmlConfigReader(
					new FallbackBytecodeClassLoader(SalveBuilder.class
							.getClassLoader(), loader));
			Config conf = new Config();

			reader.read(is, conf);
			return conf;
		} catch (ConfigException e) {
			markError(file, "Error configuring Salve: " + e.getMessage());
			throw e;
		}

	}

	private void initConfig() throws CoreException {
		try {

			config = null;
			IJavaProject jp = JavaCore.create(getProject());
			for (IClasspathEntry cpe : jp.getResolvedClasspath(true)) {
				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = cpe.getPath();
					path = path.addTrailingSeparator().append("META-INF")
							.addTrailingSeparator().append("salve.xml");
					IResource res = getProject().getWorkspace().getRoot()
							.findMember(path);
					if (res != null && res.exists()) {
						IFile file = (IFile) res;
						Config conf;
						conf = readConfig(file);
						if (conf != null) {
							config = conf;
							break;
						}
					}
				}
			}

			if (config == null) {
				markError(getProject(),
						"Could not find META-INF/salve.xml in any source folder");
			}
		} catch (ConfigException e) {
			// do nothing, error was added in readConfig()
		}

	}
}
