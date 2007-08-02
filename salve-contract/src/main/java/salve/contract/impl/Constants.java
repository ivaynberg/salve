package salve.contract.impl;

import salve.contract.NotEmpty;
import salve.contract.NotNull;
import salve.contract.OverridesMustInvoke;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.Method;

public interface Constants {
	static final Type NOTNULL = Type.getType(NotNull.class);
	static final Type NOTEMPTY = Type.getType(NotEmpty.class);

	static final Type ILLEGALARGEX = Type
			.getType(IllegalArgumentException.class);
	static final Method ILLEGALARGEX_INIT = new Method("<init>",
			"(Ljava/lang/String;)V");

	static final Type ILLEGALSTATEEX = Type
			.getType(IllegalStateException.class);

	static final Method ILLEGALSTATEEX_INIT = new Method("<init>",
			"(Ljava/lang/String;)V");

	static final Type STRING_TYPE = Type.getType(String.class);
	static final Method STRING_TRIM_METHOD = new Method("trim",
			"()Ljava/lang/String;");
	static final Method STRING_LENGTH_METHOD = new Method("length", "()I");

	static final Type OMI = Type.getType(OverridesMustInvoke.class);

}
