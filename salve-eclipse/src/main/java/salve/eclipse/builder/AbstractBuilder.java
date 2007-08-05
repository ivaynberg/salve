package salve.eclipse.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public abstract class AbstractBuilder extends IncrementalProjectBuilder {

	private final String markerType;;

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

	protected void removeMarks(IResource resource) {
		try {
			resource.deleteMarkers(markerType, true, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

}
