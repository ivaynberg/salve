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
public final class SalveInstrumentingCompiler implements ClassInstrumentingCompiler
{
// ------------------------------ FIELDS ------------------------------

  private static final Logger log = Logger.getInstance(SalveInstrumentingCompiler.class.getName());

  private static ResourceBundle MESSAGES = ResourceBundle.getBundle("salve.plugin.idea.Messages");

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
        // TODO is this the right timestamp?
        return new TimestampValidityState(file.getModificationStamp());
      }
    };
  }

  /**
   * create salve bytecode loader for current compile context
   *
   * @param context compile context for current compilation
   * @return bytecode loader for salve instrumentation
   */
  private static BytecodeLoader createBytecodeLoader(final CompileContext context)
  {
    final CompoundLoader loader = new CompoundLoader();

    for (VirtualFile rootDirectory : context.getAllOutputDirectories())
      loader.addLoader(new VirtualFileSystemBytecodeLoader(rootDirectory));

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
    for (VirtualFile outputDirectory : context.getAllOutputDirectories())
    {
      final VirtualFile classFile = VfsUtil.findRelativeFile(classPath + CLASS_FILE_EXTENSION, outputDirectory);

      if (classFile != null)
      {
        ensureFileType(context, classFile, StdFileTypes.CLASS);
        return classFile;
      }
    }
    final String message = format("classfile.locate.error", classPath);
    context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1, null);
    throw new IOException(message);
  }

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

  private static String format(final String messageKey, Object... args)
  {
    return MessageFormat.format(MESSAGES.getString(messageKey), args);
  }

// --------------------------- CONSTRUCTORS ---------------------------

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

    // get all java source files
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

    context.getProgressIndicator().pushState();

    try
    {
      context.getProgressIndicator().setText(format("status.instrumenting"));

      // setup salve
      final BytecodeLoader bytecodeLoader = createBytecodeLoader(context);
      final Map<Module, XmlConfig> salveConfigs = getSalveConfigs(context, bytecodeLoader);
      final ModificationMonitor monitor = new ModificationMonitor();

      // process each item
      for (ProcessingItem item : items)
      {
        try
        {
          final VirtualFile sourceFile = item.getFile();

          // safety check
          ensureFileType(context, sourceFile, StdFileTypes.JAVA);

          // get related class path for the current java file
          final String classPath = getSlashDelimitedClassPath(context, sourceFile);

          // check if current file has salve.xml
          final XmlConfig config = salveConfigs.get(context.getModuleByFile(sourceFile));

          // no instrumentation requested
          if (config == null)
            continue;

          // file needs to be instrumented
          for (Instrumentor instrumentor : config.getInstrumentors(classPath))
          {
            // setup context
            final InstrumentationContext ctx = new InstrumentationContext(bytecodeLoader, monitor, config.getScope(instrumentor));

            // instrument class
            final byte[] bytecode = instrumentor.instrument(classPath, ctx);

            // save bytecode
            getClassFile(context, classPath).setBinaryContent(bytecode);
          }
        }
        catch (InstrumentationException e)
        {
//        if (e instanceof salve.CodeMarkerAware)
//        {
//          CodeMarker marker = ((CodeMarkerAware) e).getCodeMarker();
//          if (marker != null)
//          {
//            line = Math.max(0, marker.getLineNumber());
//          }
//        }
//        StringBuilder message = new StringBuilder();
//        message.append("Could not instrument ").append(className).append(".");
//        if (line > 0)
//        {
//          message.append(" Error on line: ").append(line);
//        }
          // report some nice error
//        context.addMessage(CompilerMessageCategory.ERROR, );
          log.error(e.getMessage(), e);
          continue;
        }
        catch (IOException e)
        {
          log.error(e.getMessage(), e);
          continue;
        }
        // file has been instrumented successfully
        processedItems.add(item);
      }
      final String message = processedItems.size() + " files were instrumented with Salve";
      context.addMessage(CompilerMessageCategory.INFORMATION, message, null, -1, -1);
    }
    catch (ConfigException e)
    {
      log.error(e.getMessage(), e);
    }
    finally
    {
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

  private Map<Module, XmlConfig> getSalveConfigs(final CompileContext context, final BytecodeLoader loader) throws ConfigException
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

  // TODO this methods seems a little awkward - can this be done better?
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
          final String message = "file does not end with " + JAVA_FILE_EXTENSION;
          context.addMessage(CompilerMessageCategory.ERROR, message, javaFile.getUrl(), -1, -1);
          throw new IOException(message);
        }
        return javaFilePath.substring(0, javaFilePath.length() - JAVA_FILE_EXTENSION.length());
      }
    }
    final String message = "unable to get path to java file: " + javaFile.getPresentableUrl();
    context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1);
    throw new IOException(message);
  }
}
