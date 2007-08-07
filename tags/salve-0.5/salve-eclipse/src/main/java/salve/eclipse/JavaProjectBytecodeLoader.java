package salve.eclipse;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import salve.BytecodeLoader;
import salve.loader.CompoundLoader;
import salve.loader.FilePathLoader;

public class JavaProjectBytecodeLoader implements BytecodeLoader {

	private final CompoundLoader delegate;

	public JavaProjectBytecodeLoader(IProject project)
			throws JavaModelException {
		delegate = new CompoundLoader();
		Set<IProject> scanned = new HashSet<IProject>();
		addProject(project, scanned);
	}

	private void addProject(IProject project, Set<IProject> scanned)
			throws JavaModelException {
		if (!scanned.contains(project)) {
			scanned.add(project);

			IJavaProject jp = JavaCore.create(project);
			addPath(project, jp.getOutputLocation());

			for (IClasspathEntry cpe : jp.getResolvedClasspath(true)) {
				switch (cpe.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE:
					addPath(project, cpe.getOutputLocation());
					break;
				case IClasspathEntry.CPE_LIBRARY:
					addPath(project, cpe.getPath());
					break;
				case IClasspathEntry.CPE_PROJECT:
					IPath path = cpe.getPath();
					IProject other = (IProject) project.getWorkspace()
							.getRoot().findMember(path);
					addProject(other, scanned);
					break;
				case IClasspathEntry.CPE_CONTAINER:
				case IClasspathEntry.CPE_VARIABLE:
					// TODO figure out what to do about container and variable
				}
			}
		}

	}

	public byte[] loadBytecode(String className) {
		return delegate.loadBytecode(className);
	}

	private void addPath(IProject project, IPath path) {
		if (path != null) {
			IResource res = project.getWorkspace().getRoot().findMember(path);
			if (res != null) {
				File file = new File(res.getLocation().toOSString());
				if (file != null && file.exists()) {
					delegate.addLoader(new FilePathLoader(file));
				}
			} else {
				File file = new File(path.toOSString());
				if (file.exists()) {
					delegate.addLoader(new FilePathLoader(file));
				}
			}
		}
	}

}
