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
interface Constants
{
    static final String TRANSACTIONAL_DESC = "Lorg/springframework/transaction/annotation/Transactional;";
    static final String SPRINGTRANSACTIONAL_DESC = "Lsalve/depend/spring/txn/SpringTransactional;";
    
    static final String TXNATTR_NAME = "salve/depend/spring/txn/TransactionAttribute";
    static final String TXNATTR_DESC = "Lsalve/depend/spring/txn/TransactionAttribute;";
    static final String TXNATTR_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)V";
    
    static final String TXNKEY_INIT_DESC = "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)V";
    static final String TXNKEY_NAME = "salve/depend/spring/txn/TransactionalKey";
    static final String TXNKEY_DESC = "Lsalve/depend/spring/txn/TransactionalKey;";
}
