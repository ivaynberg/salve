/**
 * 
 */
package salve.contract.pe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import salve.asmlib.FieldVisitor;
import salve.asmlib.MethodVisitor;
import salve.util.asm.ClassVisitorAdapter;

final class AccessorCollectorClassVisitor extends ClassVisitorAdapter {

	private final List<Accessor> accessors = new ArrayList<Accessor>(1);

	private final String part;
	private final String getterName;
	private final String setterName;

	public AccessorCollectorClassVisitor(String part, String mode) {
		this.part = part;

		final String capped = Character.toUpperCase(part.charAt(0)) + part.substring(1);

		getterName = "get" + capped;
		setterName = "set" + capped;
	}

	public List<Accessor> getAccessors() {
		return Collections.unmodifiableList(accessors);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		accessors.clear();
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (name.equals(part)) {
			final Accessor accessor = new Accessor(Accessor.Type.FIELD, name, desc, signature);
			accessors.add(accessor);
		}
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals(getterName) && desc.startsWith("()")) {
			final Accessor accessor = new Accessor(Accessor.Type.GETTER, name, desc, signature);
			accessors.add(accessor);
			// partClassName = desc.substring(3, desc.length() - 1);
		} else if (name.equals(setterName) && desc.endsWith("V") && !desc.startsWith("()") && desc.indexOf(",") < 0) {
			final Accessor accessor = new Accessor(Accessor.Type.SETTER, name, desc, signature);
			accessors.add(accessor);
			// partClassName = desc.substring(2, desc.length() - 3);
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

}