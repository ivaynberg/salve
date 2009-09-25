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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import salve.CodeMarker;
import salve.Logger;
import salve.eclipse.Activator;
import salve.util.NoopLogger;

public abstract class AbstractBuilder extends IncrementalProjectBuilder
{

    private final String markerType;;

    public AbstractBuilder(String markerType)
    {
        this.markerType = markerType;
    }

    protected void checkCancel(IProgressMonitor monitor)
    {
        if (monitor.isCanceled())
        {
            throw new OperationCanceledException();
        }
    }

    protected void markError(IResource resource, String message, int lineNumber)
    {
        mark(resource, message, lineNumber, IMarker.SEVERITY_ERROR);
    }

    protected void markError(IResource resource, String message)
    {
        mark(resource, message, -1, IMarker.SEVERITY_ERROR);
    }

    protected void mark(IResource resource, String message, int severity)
    {
        mark(resource, message, -1, severity);
    }

    protected void mark(IResource resource, String message, int lineNumber, int severity)
    {
        try
        {
            IMarker marker = resource.createMarker(markerType);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.SEVERITY, severity);
            lineNumber = Math.max(1, lineNumber);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
        }
        catch (CoreException e)
        {
        }
    }

    protected void removeMarks(IResource resource)
    {
        try
        {
            resource.deleteMarkers(markerType, true, IResource.DEPTH_INFINITE);
        }
        catch (CoreException ce)
        {
        }
    }

    protected void log(int level, String message)
    {
        Activator.getDefault().getLog().log(new Status(level, Activator.PLUGIN_ID, message));
    }

    protected void log(int level, String message, Throwable e)
    {
        Activator.getDefault().getLog().log(new Status(level, Activator.PLUGIN_ID, message, e));
    }

    protected Logger newLogger(IResource resource)
    {
        if (resource != null)
        {
            return new EclipseLogger(resource);
        }
        else
        {
            return NoopLogger.INSTANCE;
        }
    }

    private class EclipseLogger implements Logger
    {
        private final IResource resource;

        public EclipseLogger(IResource resource)
        {
            this.resource = resource;
        }

        public void error(CodeMarker marker, String message, Object... params)
        {

            mark(resource, message(message, params), lineNumber(marker), IMarker.SEVERITY_ERROR);
        }

        public void warn(CodeMarker marker, String message, Object... params)
        {
            mark(resource, message(message, params), lineNumber(marker), IMarker.SEVERITY_ERROR);
        }

        public void info(CodeMarker marker, String message, Object... params)
        {
            mark(resource, message(message, params), lineNumber(marker), IMarker.SEVERITY_INFO);
        }

        private int lineNumber(CodeMarker marker)
        {
            return (marker != null) ? marker.getLineNumber() : -1;
        }

        private String message(String message, Object... params)
        {
            return String.format(message, params);
        }

    }

}
