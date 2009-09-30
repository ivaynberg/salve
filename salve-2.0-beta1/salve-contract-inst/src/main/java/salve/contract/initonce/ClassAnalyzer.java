package salve.contract.initonce;

import java.util.HashSet;
import java.util.Set;

import salve.Bytecode;
import salve.CannotLoadBytecodeException;
import salve.InstrumentationContext;
import salve.asmlib.AnnotationVisitor;
import salve.asmlib.ClassReader;
import salve.asmlib.FieldVisitor;
import salve.util.asm.ClassVisitorAdapter;
import salve.util.asm.FieldVisitorAdapter;

public class ClassAnalyzer {
	private static String toFieldKey(String cn, String field) {
		return new StringBuilder().append(cn).append("#").append(field).toString();
	}

	private final InstrumentationContext ctx;
	private final Set<String> analyzed;

	private final Set<String> fields;

	public ClassAnalyzer(InstrumentationContext ctx) {
		super();
		this.ctx = ctx;
		analyzed = new HashSet<String>();
		fields = new HashSet<String>();
	}

	private void analyze(final String cn) {
		Bytecode bytecode = ctx.getLoader().loadBytecode(cn);
		if (bytecode == null) {
			throw new CannotLoadBytecodeException(cn);
		}

		byte[] bytes = bytecode.getBytes();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(new ClassVisitorAdapter() {
			@Override
			public FieldVisitor visitField(int access, final String fieldName, String desc, String signature,
					Object value) {
				return new FieldVisitorAdapter() {
					@Override
					public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
						if (desc.equals("Lsalve/contract/InitOnce;")) {
							fields.add(toFieldKey(cn, fieldName));
						}
						return super.visitAnnotation(desc, visible);
					}
				};
			}
		}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
	}

	public boolean isInitOnce(String cn, String field) {
		if (!analyzed.contains(cn)) {
			analyze(cn);
		}
		return fields.contains(toFieldKey(cn, field));
	}
}
