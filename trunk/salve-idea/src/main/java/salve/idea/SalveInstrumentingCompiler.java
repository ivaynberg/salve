package salve.idea;

import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import salve.*;
import salve.config.XmlConfig;
import salve.config.XmlConfigReader;
import salve.idea.config.SalveConfiguration;
import salve.idea.util.SalveClassLoader;
import salve.idea.util.VirtualFileSystemBytecodeLoader;
import salve.loader.CompoundLoader;
import salve.monitor.ModificationMonitor;

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
// ------------------------------ FIELDS ------------------------------

  private static final Logger log = Logger.getInstance(SalveInstrumentingCompiler.class.getName());

  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("salve.idea.Messages");

  private static final ProcessingItem[] NO_PROCESSING_ITEMS = new ProcessingItem[0];

  @NonNls
  private static final String COMPILER_DESCRIPTION = "Salve Instrumenting Compiler";

  @NonNls
  private static final String JAVA_FILE_EXTENSION = ".java";

  @NonNls
  private static final String CLASS_FILE_EXTENSION = ".class";

  @NonNls
  private static final String SALVE_XML_PATH = "META-INF/salve.xml";

  // configuration for component
  private final SalveConfiguration configuration;

// -------------------------- STATIC METHODS --------------------------

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
    for (VirtualFile rootDirectory : context.getAllOutputDirectories())
      loader.addLoader(new VirtualFileSystemBytecodeLoader(rootDirectory));

    // add bytecode loaders for libraries
    for (VirtualFile libroot : LibraryUtil.getLibraryRoots(context.getProject(), false, true))
      loader.addLoader(new VirtualFileSystemBytecodeLoader(libroot));

    return loader;
  }

  /**
   * get virtual file node for class file
   *
   * @param context   compilation context
   * @param classPath path to class file
   * @return file node representing the class file
   *
   * @throws IOException is something bad happened
   */
  private static VirtualFile getClassFile(final CompileContext context, final String classPath) throws IOException
  {
    // scan output directories
    for (VirtualFile outputDirectory : context.getAllOutputDirectories())
    {
      // check if output dir contains class file
      final VirtualFile classFile = VfsUtil.findRelativeFile(classPath + CLASS_FILE_EXTENSION, outputDirectory);

      if (classFile != null)
      {
        ensureFileType(context, classFile, StdFileTypes.CLASS);
        return classFile;
      }
    }
    final String message = format("classfile.locate.error", classPath.replace('/', '.'));
    context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1, null);
    throw new IOException(message);
  }

  /**
   * make sure the specified file has the given file type (paranoia mode = true)
   *
   * @param context compilation context
   * @param file    file to check
   * @param type    expected file type
   * @throws IOException on wrong file type
   */
  private static void ensureFileType(final CompileContext context, final VirtualFile file, final FileType type) throws IOException
  {
    if (!file.getFileType().equals(type))
    {
      final Navigatable location = new OpenFileDescriptor(context.getProject(), file);
      final String message = format("file.type.unexpected.error", file.getFileType().getName(), type);
      context.addMessage(CompilerMessageCategory.ERROR, message, file.getUrl(), -1, -1, location);
      throw new IOException(message);
    }
  }

  /**
   * format message from message bundle
   *
   * @param key  message key
   * @param args message arguments
   * @return formatted message string
   */
  private static String format(final String key, Object... args)
  {
    return MessageFormat.format(MESSAGES.getString(key), args);
  }

// --------------------------- CONSTRUCTORS ---------------------------

  /**
   * create instance of salve instrumenting compiler
   *
   * @param configuration plugin configuraton
   */
  public SalveInstrumentingCompiler(final SalveConfiguration configuration)
  {
    this.configuration = configuration;
  }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Compiler ---------------------

  @NotNull
  public String getDescription()
  {
    return COMPILER_DESCRIPTION;
  }

  public boolean validateConfiguration(final CompileScope scope)
  {
    return true;
  }

// --------------------- Interface FileProcessingCompiler ---------------------

  @NotNull
  public FileProcessingCompiler.ProcessingItem[] getProcessingItems(final CompileContext context)
  {
    if (!configuration.isEnabled())
      return NO_PROCESSING_ITEMS;

    // get all java source files and report them for processing
    final VirtualFile[] files = context.getCompileScope().getFiles(StdFileTypes.JAVA, true);
    final ProcessingItem[] items = new ProcessingItem[files.length];

    for (int i = 0; i < files.length; i++)
      items[i] = createProcessingItem(files[i]);

    return items;
  }

  public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items)
  {
    // prepare processing
    final Collection<ProcessingItem> processedItems = new ArrayList<ProcessingItem>(items.length);
    int instrumentedCount = 0;

    try
    {
      // set progress indicator state
      context.getProgressIndicator().pushState();
      context.getProgressIndicator().setText(format("status.instrumenting"));

      // setup salve
      final BytecodeLoader bytecodeLoader = createBytecodeLoader(context);
      final Map<Module, XmlConfig> salveConfigs = getSalveXMLs(context, bytecodeLoader);
      final ModificationMonitor monitor = new ModificationMonitor();

      // process each item
      for (ProcessingItem item : items)
      {
        try
        {
          // get source file
          final VirtualFile sourceFile = item.getFile();

          // paranoia check
          ensureFileType(context, sourceFile, StdFileTypes.JAVA);

          // get related class path for the current java file
          final String classPath = getSlashDelimitedClassPath(context, sourceFile);

          // check if current file has salve.xml
          final Module module = context.getModuleByFile(sourceFile);
          final XmlConfig salveConfig = salveConfigs.get(module);

          if (salveConfig == null)
            continue;

          // file has related configuration in its module so it needs to be instrumented
          final Collection<Instrumentor> instrumentors = salveConfig.getInstrumentors(classPath);
          boolean instrumented = false;

          if (!instrumentors.isEmpty())
          {
            VirtualFile classFile = null;

            for (Instrumentor instrumentor : instrumentors)
            {
              // setup context
              final Scope scope = salveConfig.getScope(instrumentor);

              if (!scope.includes(classPath))
                continue;

              final InstrumentationContext ctx = new InstrumentationContext(bytecodeLoader, monitor, scope);

              try
              {
                if (classFile == null)
                  classFile = getClassFile(context, classPath);

                // instrument class
                classFile.setBinaryContent(instrumentor.instrument(classPath, ctx));

                // class has been changed
                instrumented = true;
              }
              catch (InstrumentationException e)
              {
                final String className = classPath.replace('/', '.');

                final StringBuilder message = new StringBuilder();
                message.append(format("instrumentation.failed", className));

                CodeMarker marker = e.getCodeMarker();

                int line = -1;

                if (marker != null)
                {
                  line = Math.max(-1, marker.getLineNumber());

                  if (line > -1)
                    message.append(' ').append(format("instrumentation.failed.lineNumber", line));
                }

                final Navigatable location = new OpenFileDescriptor(context.getProject(), sourceFile, line, 0);
                context.addMessage(CompilerMessageCategory.ERROR, message.toString(), sourceFile.getUrl(), line, 0, location);
                log.error(e.getMessage(), e);
              }
            }
            if (instrumented)
              instrumentedCount++;
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
      final String message = format("instrumentation.done", processedItems.size(), instrumentedCount);
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
    return processedItems.toArray(new ProcessingItem[processedItems.size()]);
  }

// --------------------- Interface ValidityStateFactory ---------------------

  public ValidityState createValidityState(final DataInput in) throws IOException
  {
    return new TimestampValidityState(System.currentTimeMillis());
  }

// -------------------------- OTHER METHODS --------------------------

  private Map<Module, XmlConfig> getSalveXMLs(final CompileContext context, final BytecodeLoader loader) throws ConfigException
  {
    // allows loading of instrumentors from project libraries
    final SalveClassLoader classLoader = new SalveClassLoader(getClass().getClassLoader(), loader);

    // xml config reader for salve.xml
    final XmlConfigReader salveXmlReader = new XmlConfigReader(classLoader);

    // scan for salve.xml on the modules
    final Module[] modules = context.getCompileScope().getAffectedModules();
    final Map<Module, XmlConfig> map = new HashMap<Module, XmlConfig>(modules.length);

    for (final Module module : modules)
    {
      for (VirtualFile sourceRoot : context.getSourceRoots(module))
      {
        final VirtualFile salveXml = VfsUtil.findRelativeFile(SALVE_XML_PATH, sourceRoot);

        if (salveXml == null)
          continue;

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
        break;
      }
    }
    return map;
  }

  private String getSlashDelimitedClassPath(final CompileContext context, final VirtualFile javaFile) throws IOException
  {
    // get associated module for java file
    final Module module = context.getModuleByFile(javaFile);

    // get source roots of the current file's module
    for (VirtualFile sourceRoot : context.getSourceRoots(module))
    {
      // check which of the source root contains the java file
      final String javaFilePath = VfsUtil.getPath(sourceRoot, javaFile, '/');

      // get the path to and strip away '.java'
      if (javaFilePath != null)
      {
        if (!javaFilePath.endsWith(JAVA_FILE_EXTENSION))
        {
          final String message = format("file.wrong.extension", JAVA_FILE_EXTENSION);
          context.addMessage(CompilerMessageCategory.ERROR, message, javaFile.getUrl(), -1, -1);
          throw new IOException(message);
        }
        return javaFilePath.substring(0, javaFilePath.length() - JAVA_FILE_EXTENSION.length());
      }
    }
    final String message = format("filepath.unresolvable", javaFile.getPresentableUrl());
    context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1);
    throw new IOException(message);
  }
}
