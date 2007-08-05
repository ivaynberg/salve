package salve.depend.impl;

public interface Constants {
	public static final String NS = "_salve";
	public static final String DEPNS = NS + "dep";
	public static final String KEY_FIELD_PREFIX = DEPNS + "key$";
	public static final String FIELDINIT_METHOD_PREFIX = DEPNS + "fldinit$";
	public static final String CLINIT_METHOD_PREFIX = DEPNS + "clinit$";

	public static final String KEYIMPL_NAME = "salve/depend/KeyImpl";
	public static final String KEYIMPL_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/String;)V";
	public static final String DEPLIB_NAME = "salve/depend/DependencyLibrary";
	public static final String KEY_NAME = "salve/depend/Key";
	public static final String KEY_DESC = "L" + KEY_NAME + ";";
	public static final String DEPLIB_LOCATE_METHOD_DESC = "(Lsalve/depend/Key;)Ljava/lang/Object;";
	public static final String DEPLIB_LOCATE_METHOD = "locate";
	public static final String IFWE_NAME = "salve/depend/IllegalFieldWriteException";
	public static final String IFWE_INIT_DESC = "(Ljava/lang/String;Ljava/lang/String;)V";

	public static final String DEP_DESC = "Lsalve/depend/Dependency;";
	// import salve.dependency.InjectionStrategy;

	public static final String STRAT_REMOVE = "REMOVE_FIELD";
	public static final String STRAT_INJECT = "INJECT_FIELD";

}
