package salve.model;

import salve.asmlib.Opcodes;
import salve.asmlib.Type;

class Method {
	private final String name;
	private final int access;
	private final String desc;

	public Method(int access, String name, String desc) {
		this.desc = desc;
		this.access = access;
		this.name = name;
	}

	public boolean canOverride(Method other) {
		if (other.isPublic()) {
			if (!isPublic()) {
				return false;
			}
		}

		if (other.isProtected()) {
			if (!isPublic() && !isProtected()) {
				return false;
			}
		}

		if (other.isPrivate()) {
			return false;
		}
		if (other.isPackagePrivate()) {
			if (isPrivate()) {
				return false;
			}
			if (isPackagePrivate()) {
				// FIXME check packages are the same?
				return true;
			} else {
				return false;
			}

		}

		if (!other.name.equals(name)) {
			return false;
		}
		Type[] args = Type.getArgumentTypes(desc);
		Type[] otherArgs = Type.getArgumentTypes(desc);
		if (args != otherArgs) {
			if (args == null || otherArgs == null) {
				return false;
			}
			if (args.length != otherArgs.length) {
				return false;
			}
			for (int i = 0; i < args.length; i++) {
				if (!args[i].equals(otherArgs[i])) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Method other = (Method) obj;
		if (access != other.access) {
			return false;
		}
		if (desc == null) {
			if (other.desc != null) {
				return false;
			}
		} else if (!desc.equals(other.desc)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public Type getReturnType() {
		return Type.getReturnType(desc);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + access;
		result = prime * result + (desc == null ? 0 : desc.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	public boolean isPackagePrivate() {
		return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE)) == 0;
	}

	public boolean isPrivate() {
		return (access & Opcodes.ACC_PRIVATE) > 0;
	}

	public boolean isProtected() {
		return (access & Opcodes.ACC_PROTECTED) > 0;
	}

	public boolean isPublic() {
		return (access & Opcodes.ACC_PUBLIC) > 0;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (isPublic()) {
			str.append("pubic ");
		} else if (isProtected()) {
			str.append("protected ");
		} else if (isPrivate()) {
			str.append("private ");
		}

		if ((access & Opcodes.ACC_STATIC) > 0) {
			str.append("static ");
		}
		str.append(name).append(" ").append(desc);
		return str.toString();

	}

}
