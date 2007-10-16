package salve.util;

import java.util.HashMap;

import salve.BytecodeLoader;
import salve.asmlib.ClassReader;
import salve.asmlib.ClassWriter;
import salve.asmlib.Opcodes;

/**
 * {@link ClassWriter} with an implementation of
 * {@link #getCommonSuperClass(String, String)} that uses the provided
 * {@link BytecodeLoader}. The problem with the default implementation is that
 * it tries to use {@link Class#forName(String)} to test the hierarchy which is
 * not possible because the current classloader might not have access to
 * requested classes.
 * 
 * @author ivaynberg
 * 
 */
public class BytecodeLoadingClassWriter extends ClassWriter {
	private class ClassInfo {
		private final String name;
		private ClassInfo superInfo;
		private ClassInfo[] interfaceInfo;
		private final boolean isInterface;

		public ClassInfo(String type, BytecodeLoader loader) {
			byte[] bytecode = loader.loadBytecode(type);
			if (bytecode == null) {
				throw new IllegalStateException("Bytecode loader could not load bytecode for type: " + type);
			}
			ClassReader reader = new ClassReader(bytecode);
			this.name = reader.getClassName();
			String superType = reader.getSuperName();
			if (superType != null) {
				superInfo = typeNameToClassInfo.get(superType);
				if (superInfo == null) {
					superInfo = new ClassInfo(superType, loader);
					typeNameToClassInfo.put(superType, superInfo);
				}
			}
			String[] interfaceTypes = reader.getInterfaces();
			if (interfaceTypes != null && interfaceTypes.length > 0) {
				interfaceInfo = new ClassInfo[interfaceTypes.length];
				for (int i = 0; i < interfaceTypes.length; i++) {
					final String iname = interfaceTypes[i];
					interfaceInfo[i] = typeNameToClassInfo.get(iname);
					if (interfaceInfo[i] == null) {
						interfaceInfo[i] = new ClassInfo(iname, loader);
						typeNameToClassInfo.put(iname, interfaceInfo[i]);
					}
				}
			}
			isInterface = (reader.getAccess() & Opcodes.ACC_INTERFACE) > 0;
		}

		private boolean implementsInterface(final ClassInfo that) {
			for (ClassInfo c = this; c != null; c = c.superInfo) {
				ClassInfo[] tis = c.interfaceInfo;
				if (tis != null) {
					for (int i = 0; i < tis.length; ++i) {
						ClassInfo ti = tis[i];
						if (ti.name.equals(that.name) || ti.implementsInterface(that)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public boolean isAssignableFrom(final ClassInfo that) {
			if (this == that) {
				return true;
			}

			if (name.equals(that.name)) {
				return true;
			}

			if (that.isSubclassOf(this)) {
				return true;
			}

			if (that.implementsInterface(this)) {
				return true;
			}

			if (that.isInterface && name.equals("java/lang/Object")) {
				return true;
			}

			return false;
		}

		public boolean isSubclassOf(final ClassInfo that) {
			for (ClassInfo c = this; c != null; c = c.superInfo) {
				if (c.superInfo != null && c.superInfo.name.equals(that.name)) {
					return true;
				}
			}
			return false;
		}

	}

	private final HashMap<String, ClassInfo> typeNameToClassInfo = new HashMap<String, ClassInfo>();

	private final BytecodeLoader loader;

	public BytecodeLoadingClassWriter(ClassReader classReader, int flags, BytecodeLoader loader) {
		super(classReader, flags);
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	public BytecodeLoadingClassWriter(int flags, BytecodeLoader loader) {
		super(flags);
		if (loader == null) {
			throw new IllegalArgumentException("Argument `loader` cannot be null");
		}
		this.loader = loader;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		ClassInfo c = new ClassInfo(type1, loader);
		ClassInfo d = new ClassInfo(type2, loader);
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface || d.isInterface) {
			return "java/lang/Object";
		} else {
			do {
				c = c.superInfo;
			} while (!c.isAssignableFrom(d));
			return c.name;
		}
	}
}
