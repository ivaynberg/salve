package salve.idea.v2.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import salve.BytecodeLoader;
import salve.Bytecode;

import java.io.IOException;

/**
 * byteloader that loads .class file binary data from IDEA's virtual file system
 *
 * @author Peter Ertl
 */
public final class VirtualFileSystemBytecodeLoader implements BytecodeLoader
{
	private static final Logger log = Logger.getInstance(VirtualFileSystemBytecodeLoader.class.getName());
	private static final String CLASS_FILE_EXTENSION = ".class";

	// root virtual file system node for class file searching
	private final VirtualFile root;

	public VirtualFileSystemBytecodeLoader(final VirtualFile root)
	{
		this.root = root;
	}

	/**
	 * locate bytecode for java class in virtual file system
	 *
	 * @param classPath class path to java class (slash-separated, no '.class' extension)
	 * @return bytecode for class or <code>null</code> if not found
	 */
	public Bytecode loadBytecode(final String classPath)
	{
		try
		{
			final VirtualFile virtualFile = VfsUtil.findRelativeFile(classPath + CLASS_FILE_EXTENSION, root);

			if (virtualFile == null)
				return null;

			return new Bytecode(classPath, virtualFile.contentsToByteArray(), this);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
		return null;
	}
}

