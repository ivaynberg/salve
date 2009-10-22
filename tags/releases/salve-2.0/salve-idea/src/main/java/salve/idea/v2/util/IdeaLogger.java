package salve.idea.v2.util;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import salve.CodeMarker;
import salve.Logger;

public class IdeaLogger implements Logger
{
	private final CompileContext context;
	private final VirtualFile sourceFile;

	public IdeaLogger(final CompileContext context, final VirtualFile sourceFile)
	{
		this.context = context;
		this.sourceFile = sourceFile;
	}

	private void addMessage(final CompilerMessageCategory category, final CodeMarker marker, final String message)
	{
		final int lineNumber = marker.getLineNumber();
		final Navigatable location = new OpenFileDescriptor(context.getProject(), sourceFile, lineNumber, 0);
		context.addMessage(category, message, sourceFile.getUrl(), lineNumber, 0, location);
	}

	public void error(final CodeMarker marker, final String message, final Object... params)
	{
		addMessage(CompilerMessageCategory.ERROR, marker, message);
	}

	public void warn(final CodeMarker marker, final String message, final Object... params)
	{
		addMessage(CompilerMessageCategory.WARNING, marker, message);
	}

	public void info(final CodeMarker marker, final String message, final Object... params)
	{
		addMessage(CompilerMessageCategory.INFORMATION, marker, message);
	}
}
