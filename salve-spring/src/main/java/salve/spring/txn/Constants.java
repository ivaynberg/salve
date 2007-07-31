package salve.spring.txn;

public interface Constants {
	static final String TRANSACTIONAL_DESC = "Lorg/springframework/transaction/annotation/Transactional;";
	static final String SPRINGTRANSACTIONAL_DESC = "Lsalve/spring/txn/SpringTransactional;";
	static final String PTM_NAME = "org/springframework/transaction/PlatformTransactionManager";
	static final String PTM_DESC = "Lorg/springframework/transaction/PlatformTransactionManager;";
	static final String PTM_GETTXN_METHOD_NAME = "getTransaction";
	static final String PTM_GETTXN_METHOD_DESC = "(Lorg/springframework/transaction/TransactionDefinition;)Lorg/springframework/transaction/TransactionStatus;";
	static final String STATUS_DESC = "Lorg/springframework/transaction/TransactionStatus;";
	static final String ADVISERUTIL_NAME = "salve/spring/txn/AdviserUtil";
	static final String ADVISERUTIL_LOCATE_METHOD_NAME = "locateTransactionManager";
	static final String ADVISERUTIL_LOCATE_METHOD_DESC = "()Lorg/springframework/transaction/PlatformTransactionManager;";
	static final String ADVISERUTIL_COMPLETE_METHOD_NAME = "complete";
	static final String ADVISERUTIL_COMPLETE_METHOD_DESC = "(Lorg/springframework/transaction/PlatformTransactionManager;Lorg/springframework/transaction/TransactionStatus;Lsalve/spring/txn/TransactionAttribute;)V";
	static final String TXNATTR_NAME = "salve/spring/txn/TransactionAttribute";
	static final String TXNATTR_DESC = "Lsalve/spring/txn/TransactionAttribute;";
	static final String TXNATTR_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)V";
}
