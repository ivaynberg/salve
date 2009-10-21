package salve.idea.v2;

import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import salve.*;
import salve.config.xml.XmlConfig;
import salve.config.xml.XmlConfigReader;
import salve.idea.v2.config.SalveConfiguration;
import salve.idea.v2.util.IdeaLogger;
import salve.idea.v2.util.SalveClassLoader;
import salve.idea.v2.util.VirtualFileSystemBytecodeLoader;
import salve.loader.CompoundLoader;
import salve.model.CtProject;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * Salve bytecode instrumentor
 *
 * @author Peter Ertl
 */
final class SalveInstrumentingCompiler implements ClassInstrumentingCompiler
{
	private static final Logger log = Logger.getInstance(SalveInstrumentingCompiler.class.getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("salve.idea.v2.Messages");
	private static final ProcessingItem[] NO_PROCESSING_ITEMS = new ProcessingItem[0];

	private static final String COMPILER_DESCRIPTION = "Salve Instrumenting Compiler";
	private static final String JAVA_FILE_EXTENSION = ".java";
	private static final String CLASS_FILE_EXTENSION = ".class";
	private static final String SALVE2_XML_PATH = "META-INF/salve2.xml";
	private static final String PACKAGE_DESCRIPTOR = "package-info.java";

	// timestamp of last instrumentation
	private long lastTimestamp = Long.MIN_VALUE;

	// configuration for component
	private final SalveConfiguration configuration;

	private static ProcessingItem createProcessingItem(final VirtualFile file)
	{
		return new ProcessingItem()
		{
			@NotNull
			public VirtualFile getFile()
			{
				return file;
			}

			public ValidityState getValidityState()
			{
				return new TimestampValidityState(file.getModificationStamp());
			}
		};
	}

	/**
	 * create salve bytecode loader for current compilation context
	 * <p/>
	 * this loader will be able to load compiled classes from the project and its attached libraries
	 *
	 * @param context compile context for current compilation
	 * @return bytecode loader for instrumentation
	 */
	private static BytecodeLoader createBytecodeLoader(final CompileContext context)
	{
		// compound loaders aggregate several bytecode loaders
		final CompoundLoader loader = new CompoundLoader();

		// add bytecode loaders for compiled classes
		for (final VirtualFile rootDirectory : context.getAllOutputDirectories())
			loader.addLoader(new VirtualFileSystemBytecodeLoader(rootDirectory));

		// add bytecode loaders for libraries
		for (final VirtualFile libroot : LibraryUtil.getLibraryRoots(context.getProject(), false, true))
			loader.addLoader(new VirtualFileSystemBytecodeLoader(libroot));

		return loader;
	}

	/**
	 * get virtual file node for class file
	 *
	 * @param context   compilation context
	 * @param classPath path to class file
	 * @return file node representing the class file or <code>null</code> if class file not found
	 *
	 * @throws IOException if something bad happened
	 */
	private static VirtualFile getClassFile(final CompileContext context, final String classPath) throws IOException
	{
		// scan output directories
		for (final VirtualFile outputDirectory : context.getAllOutputDirectories())
		{
			// check if output dir contains class file
			final VirtualFile classFile = VfsUtil.findRelativeFile(classPath + CLASS_FILE_EXTENSION, outputDirectory);

			if (classFile != null)
			{
				// paranoia = 1
				if (!classFile.getFileType().equals(StdFileTypes.CLASS))
				{
					final String error = MessageFormat.format("file {0} has wrong type {1} ",
					                                          classFile.getPresentableUrl(), classFile.getFileType().getName());
					throw new IOException(error);
				}
				return classFile;
			}
		}
		return null;
	}

	/**
	 * format message from message bundle
	 *
	 * @param key  message key
	 * @param args message arguments
	 * @return formatted message string
	 */
	private static String format(final String key, final Object... args)
	{
		return MessageFormat.format(MESSAGES.getString(key), args);
	}

	/**
	 * create instance of salve instrumenting compiler
	 *
	 * @param configuration plugin configuraton
	 */
	public SalveInstrumentingCompiler(final SalveConfiguration configuration)
	{
		this.configuration = configuration;
	}

	@NotNull
	public String getDescription()
	{
		return COMPILER_DESCRIPTION;
	}

	public boolean validateConfiguration(final CompileScope scope)
	{
		return true;
	}

	@NotNull
	public FileProcessingCompiler.ProcessingItem[] getProcessingItems(final CompileContext context)
	{
		if (!configuration.isEnabled())
			return NO_PROCESSING_ITEMS;

		// get all java source files and report them for processing
		final VirtualFile[] files = context.getCompileScope().getFiles(StdFileTypes.JAVA, true);
		final Collection<ProcessingItem> items = new ArrayList<ProcessingItem>(files.length);

		for (final VirtualFile file : files)
		{
			if (PACKAGE_DESCRIPTOR.equals(file.getName()))
				continue;

			items.add(createProcessingItem(file));
		}
		return items.toArray(new ProcessingItem[items.size()]);
	}

	public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items)
	{
		// prepare processing
		final Collection<ProcessingItem> processedItems = new ArrayList<ProcessingItem>(items.length);
		long currentTimestamp = lastTimestamp;

		try
		{
			// set progress indicator state
			context.getProgressIndicator().pushState();
			context.getProgressIndicator().setText(format("status.instrumenting"));

			// setup salve
			final BytecodeLoader loader = createBytecodeLoader(context);
			final Map<Module, XmlConfig> salveConfigs = getSalveXMLs(context, loader);

			// access to project files
			final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(context.getProject()).getFileIndex();

			// process each item
			for (final ProcessingItem item : items)
			{
				try
				{
					// get source file
					final VirtualFile sourceFile = item.getFile();

					// don't even know if this could ever happen?! (paranoia = 1)
					if (!projectFileIndex.isContentJavaSourceFile(sourceFile))
						continue;

					// get class path for the current java file
					final String classPath = getClassPath(projectFileIndex, sourceFile);

					// check if current file has salve.xml
					final Module module = context.getModuleByFile(sourceFile);
					final XmlConfig salveConfig = salveConfigs.get(module);

					if (salveConfig == null)
						continue;

					// file has related configuration in its module so it needs to be instrumented
					final Collection<Instrumentor> instrumentors = salveConfig.getInstrumentors(classPath);

					if (!instrumentors.isEmpty())
					{
						final VirtualFile classFile = getClassFile(context, classPath);

						if (classFile == null)
						{
							final String message = format("classfile.missing.rebuild.requested", classPath.replace('/', '.'));
							log.info(message);
							context.requestRebuildNextTime(message);
							break;
						}

						// ignore class file if it was not modified since last instrumentation run
						if (classFile.getModificationCount() <= lastTimestamp)
							continue;

						for (final Instrumentor instrumentor : instrumentors)
						{
							// setup context
							final Scope scope = salveConfig.getScope(instrumentor);

							if (scope.includes(classPath) == false)
								continue;

							final CtProject model = new CtProject().setLoader(loader);
							final IdeaLogger logger = new IdeaLogger(context, sourceFile);
							final InstrumentationContext ctx = new InstrumentationContext(loader, scope, model, logger);

							try
							{
								// instrument class
								final byte[] bytes = instrumentor.instrument(classPath, ctx);

								// save class
								final long now = System.currentTimeMillis();
								classFile.setBinaryContent(bytes, now, now);

								// keep track of latest modification timestamp of any class file by this instrumentor
								// anything beyond that point is considered to be changed and
								// needs instrumentation in any future run
								currentTimestamp = Math.max(currentTimestamp, classFile.getModificationCount());
							}
							catch (InstrumentationException e)
							{
								final StringBuilder message = new StringBuilder();
								message.append(format("instrumentation.failed", classPath.replace('/', '.')));

								int line = -1;

								if (e instanceof CodeMarkerAware)
								{
									final CodeMarker marker = e.getCodeMarker();

									if (marker != null)
									{
										line = Math.max(-1, marker.getLineNumber());

										if (line > -1)
											message.append(' ').append(format("instrumentation.failed.lineNumber", line));
									}
								}
								final Navigatable location = new OpenFileDescriptor(context.getProject(), sourceFile, line, 0);
								context.addMessage(CompilerMessageCategory.ERROR, message.toString(), sourceFile.getUrl(), line, 0, location);
								log.error(e.getMessage(), e);
							}
						}
					}
				}
				catch (IOException e)
				{
					log.error(e.getMessage(), e);
					continue;
				}
				// file has been instrumented successfully
				processedItems.add(item);
			}
			final String message = format("instrumentation.done", processedItems.size());
			context.addMessage(CompilerMessageCategory.INFORMATION, message, null, -1, -1);
		}
		catch (ConfigException e)
		{
			log.error(e.getMessage(), e);
		}
		finally
		{
			// restore progress indicator state
			context.getProgressIndicator().popState();
		}
		// update timestamp of last compile
		lastTimestamp = currentTimestamp;

		return processedItems.toArray(new ProcessingItem[processedItems.size()]);
	}

	public ValidityState createValidityState(final DataInput in) throws IOException
	{
		return new TimestampValidityState(System.currentTimeMillis());
	}

	private Map<Module, XmlConfig> getSalveXMLs(final CompileContext context, final BytecodeLoader loader) throws ConfigException
	{
		// allows loading of instrumentors from project libraries
		final SalveClassLoader classLoader = new SalveClassLoader(SalveInstrumentingCompiler.class.getClassLoader(), loader);

		// xml config reader for salve.xml
		final XmlConfigReader salveXmlReader = new XmlConfigReader(classLoader);

		// scan for salve.xml on the modules
		final Module[] modules = context.getCompileScope().getAffectedModules();
		final Map<Module, XmlConfig> map = new HashMap<Module, XmlConfig>(modules.length);

		for (final Module module : modules)
		{
			for (final VirtualFile sourceRoot : context.getSourceRoots(module))
			{
				final VirtualFile salveXml = VfsUtil.findRelativeFile(SALVE2_XML_PATH, sourceRoot);

				if (salveXml == null)
					continue;

				if (map.containsKey(module))
				{
					final String message = format("module.multiple.configs", module.getName(), SALVE2_XML_PATH);
					context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1, null);
					throw new ConfigException(message);
				}

				final XmlConfig config = new XmlConfig();
				InputStream inputStream = null;

				try
				{
					inputStream = salveXml.getInputStream();
					salveXmlReader.read(inputStream, config);
				}
				catch (Exception e)
				{
					final Navigatable location = new OpenFileDescriptor(context.getProject(), salveXml);
					context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), salveXml.getUrl(), -1, -1, location);
					throw new ConfigException(e.getMessage(), e);
				}
				finally
				{
					try
					{
						if (inputStream != null)
							inputStream.close();
					}
					catch (IOException e)
					{
						log.error(e.getMessage(), e);
					}
				}
				map.put(module, config);
			}
		}
		return map;
	}

	private String getClassPath(final ProjectFileIndex index, final VirtualFile javaFile) throws IOException
	{
		final VirtualFile sourceRoot = index.getSourceRootForFile(javaFile);

		if (sourceRoot == null)
			throw new IOException("file [" + javaFile + "] has no source root");

		final String javaFilePath = VfsUtil.getRelativePath(javaFile, sourceRoot, '/');

		// get the path to and strip away '.java'
		if (javaFilePath == null)
			throw new IOException(format("filepath.unresolvable", javaFile.getPresentableUrl()));

		if (!javaFilePath.endsWith(JAVA_FILE_EXTENSION))
			throw new IOException(format("file.wrong.extension", JAVA_FILE_EXTENSION));

		return javaFilePath.substring(0, javaFilePath.length() - JAVA_FILE_EXTENSION.length());
	}
}
