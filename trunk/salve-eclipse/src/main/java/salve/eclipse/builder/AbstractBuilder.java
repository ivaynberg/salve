package salve.eclipse.builder;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractBuilder extends IncrementalProjectBuilder {

	class DeltaVisitor implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				return build(resource);
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				return build(resource);
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class ResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) throws CoreException {
			return build(resource);
		}
	}

	private final String markerType;;

	private int buildKind;

	private Map buildArgs;

	private IProgressMonitor buildMonitor;

	protected int getBuildKind() {
		return buildKind;
	}

	public AbstractBuilder(String markerType) {
		this.markerType = markerType;
	}

	protected void markError(IResource resource, String message) {
		System.out.println("marking error on: " + resource + " with message: "
				+ message);
		mark(resource, message, -1, IMarker.SEVERITY_ERROR);
	}

	protected void mark(IResource resource, String message, int severity) {
		mark(resource, message, -1, severity);
	}

	protected void mark(IResource resource, String message, int lineNumber,
			int severity) {
		try {
			IMarker marker = resource.createMarker(markerType);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			lineNumber = Math.min(1, lineNumber);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		this.buildKind = kind;
		this.buildArgs = args;
		this.buildMonitor = monitor;
		onStartBuild();
		try {

			if (kind == FULL_BUILD) {
				getProject().accept(new ResourceVisitor());
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					getProject().accept(new ResourceVisitor());
				} else {
					delta.accept(new DeltaVisitor());
				}
			}
		} finally {
			onEndBuild();
		}
		return null;
	}

	private boolean build(IResource resource) throws CoreException {
		IMarker[] projectErrors = getProject().findMarkers(markerType, true,
				IResource.DEPTH_ZERO);
		if (projectErrors == null || projectErrors.length == 0) {
			onBuild(resource);
			return true;
		} else {
			return false;
		}
	}

	protected void removeMarks(IResource resource) {
		try {
			resource.deleteMarkers(markerType, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected abstract void onStartBuild() throws CoreException;

	protected abstract void onEndBuild() throws CoreException;

	protected abstract void onBuild(IResource resource) throws CoreException;

	protected Map getBuildArgs() {
		return buildArgs;
	}

	protected IProgressMonitor getBuildMonitor() {
		return buildMonitor;
	}

}
