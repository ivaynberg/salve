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
import salve.Config;
import salve.ConfigException;
import salve.InstrumentationException;
import salve.Instrumentor;
import salve.asmlib.ClassReader;
import salve.config.XmlConfig;
import salve.config.XmlConfigReader;
import salve.eclipse.Activator;
import salve.eclipse.JavaProjectBytecodeLoader;
import salve.loader.CompoundLoader;
import salve.monitor.NoopMonitor;
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
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		removeMarks(getProject());

		// find config resource
		final IResource configResource = findConfig();
		if (configResource == null) {
			markError(getProject(),
					"Could not find META-INF/salve.xml in any source folder");
			return null;
		}
		checkCancel(monitor);
		removeMarks(configResource);

		// load config
		BytecodeLoader bloader = new JavaProjectBytecodeLoader(getProject());
		ClassLoader cloader = new FallbackBytecodeClassLoader(getClass()
				.getClassLoader(), bloader);

		final XmlConfig config = new XmlConfig();
		XmlConfigReader reader = new XmlConfigReader(cloader);
		try {
			reader.read(((IFile) configResource).getContents(), config);
		} catch (ConfigException e) {
			markError(configResource, "Could not configure Salve: "
					+ e.getMessage());
			return null;
		}

		checkCancel(monitor);

		// check if we need to upgrade the build to full
		if (kind != FULL_BUILD) {
			final boolean[] modified = new boolean[] { false };
			getDelta(getProject()).accept(new IResourceDeltaVisitor() {

				public boolean visit(IResourceDelta delta) throws CoreException {
					if (delta.getResource().equals(configResource)) {
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
						case IResourceDelta.CHANGED:
							modified[0] = true;
							break;
						case IResourceDelta.REMOVED:

							break;

						}
						return false;
					}
					return true;
				}
			});

			if (modified[0]) {
				// config file was modified, upgrade build to full
				kind = FULL_BUILD;
			}
		}

		// build
		ResourceBuilder builder = new ResourceBuilder(config, bloader, monitor);
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

	private IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
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

	class ResourceBuilder implements IResourceVisitor, IResourceDeltaVisitor {
		private final Config config;
		private final BytecodeLoader bloader;
		private final IProgressMonitor monitor;

		public ResourceBuilder(Config config, BytecodeLoader bloader,
				IProgressMonitor monitor) {
			super();
			this.config = config;
			this.bloader = bloader;
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) throws CoreException {
			build(resource, config, bloader, monitor);
			return true;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				build(delta.getResource(), config, bloader, monitor);
				break;
			case IResourceDelta.REMOVED:
				break;
			}
			return true;
		}
	}

	public void build(IResource resource, Config config,
			BytecodeLoader bloader, IProgressMonitor monitor)
			throws CoreException {

		checkCancel(monitor);

		if (!(resource instanceof IFile)
				|| !resource.getName().endsWith(".class")) {
			return;
		}

		final IFile file = (IFile) resource;

		removeMarks(resource);
		try {

			ClassReader reader;
			reader = new ClassReader(file.getContents());
			final String cn = reader.getClassName();
			for (Instrumentor inst : config.getInstrumentors(cn)) {
				// System.out.println("instrumenting: " + cn + " with: "
				// + inst.getClass().getName());
				CompoundLoader cl = new CompoundLoader();
				cl.addLoader(new FileBytecodeLoader(file));
				cl.addLoader(bloader);
				byte[] bytecode = inst.instrument(cn, cl, NoopMonitor.INSTANCE);
				file.setContents(new ByteArrayInputStream(bytecode), true,
						false, null);
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
