/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package salve.depend.spring.txn;

/**
 * INTERNAL
 * <p>
 * Constants used by class instrumentor
 * </p>
 * 
 * @author ivaynberg
 */
interface Constants {
	static final String TRANSACTIONAL_DESC = "Lorg/springframework/transaction/annotation/Transactional;";
	static final String SPRINGTRANSACTIONAL_DESC = "Lsalve/depend/spring/txn/SpringTransactional;";
	static final String PTM_NAME = "org/springframework/transaction/PlatformTransactionManager";
	static final String PTM_DESC = "Lorg/springframework/transaction/PlatformTransactionManager;";
	static final String PTM_GETTXN_METHOD_NAME = "getTransaction";
	static final String PTM_GETTXN_METHOD_DESC = "(Lorg/springframework/transaction/TransactionDefinition;)Lorg/springframework/transaction/TransactionStatus;";
	static final String STATUS_DESC = "Lorg/springframework/transaction/TransactionStatus;";
	static final String ADVISERUTIL_NAME = "salve/depend/spring/txn/AdviserUtil";
	static final String ADVISERUTIL_LOCATE_METHOD_NAME = "locateTransactionManager";
	static final String ADVISERUTIL_LOCATE_METHOD_DESC = "()Lorg/springframework/transaction/PlatformTransactionManager;";
	static final String ADVISERUTIL_COMPLETE_METHOD_NAME = "complete";
	static final String ADVISERUTIL_COMPLETE_METHOD_DESC = "(Lorg/springframework/transaction/PlatformTransactionManager;Lorg/springframework/transaction/TransactionStatus;Lsalve/depend/spring/txn/TransactionAttribute;)V";
	static final String ADVISERUTIL_COMPLETE_METHOD_DESC2 = "(Ljava/lang/Throwable;Lorg/springframework/transaction/PlatformTransactionManager;Lorg/springframework/transaction/TransactionStatus;Lsalve/depend/spring/txn/TransactionAttribute;)V";
	static final String TXNATTR_NAME = "salve/depend/spring/txn/TransactionAttribute";
	static final String TXNATTR_DESC = "Lsalve/depend/spring/txn/TransactionAttribute;";
	static final String TXNATTR_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)V";
}
