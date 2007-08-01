package salve.contract.impl;

import salve.contract.NotEmpty;
import salve.contract.NotNull;
import salve.org.objectweb.asm.Type;
import salve.org.objectweb.asm.commons.Method;

public interface Constants {
	static final String NOTNULL_DESC = Type.getType(NotNull.class)
			.getDescriptor();
	static final String NOTEMPTY_DESC = Type.getType(NotEmpty.class)
			.getDescriptor();

	static final Type ILLEGALARGEX = Type
			.getType(IllegalArgumentException.class);
	static final Method ILLEGALARGEX_INIT = new Method("<init>",
			"(Ljava/lang/String;)V");

	static final Type ILLEGALSTATEEX = Type
			.getType(IllegalStateException.class);

	static final Method ILLEGALSTATEEX_INIT = new Method("<init>",
			"(Ljava/lang/String;)V");

}
