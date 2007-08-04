package salve.dependency.impl;

public interface Constants {
	public static final String NS = "_salve";
	public static final String DEPNS = NS + "dep";
	public static final String KEY_FIELD_PREFIX = DEPNS + "key$";
	public static final String FIELDINIT_METHOD_PREFIX = DEPNS + "fldinit$";
	public static final String CLINIT_METHOD_PREFIX = DEPNS + "clinit$";

	public static final String KEYIMPL_NAME = "salve/dependency/KeyImpl";
	public static final String KEYIMPL_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V";
	public static final String DEPLIB_NAME = "salve/dependency/DependencyLibrary";
	public static final String KEY_NAME = "salve/dependency/Key";
	public static final String KEY_DESC = "L" + KEY_NAME + ";";
	public static final String DEPLIB_LOCATE_METHOD_DESC = "(Lsalve/dependency/Key;)Ljava/lang/Object;";
	public static final String DEPLIB_LOCATE_METHOD = "locate";
	public static final String IFWE_NAME = "salve/dependency/IllegalFieldWriteException";
	public static final String IFWE_INIT_DESC = "(Ljava/lang/String;Ljava/lang/String;)V";

	public static final String DEP_DESC = "Lsalve/dependency/Dependency;";
	// import salve.dependency.InjectionStrategy;

	public static final String STRAT_REMOVE = "REMOVE_FIELD";
	public static final String STRAT_INJECT = "INJECT_FIELD";

}
