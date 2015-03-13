# Introduction #

One of the best features of Spring introduced in 2.0 is the @Transactional annotation which allows an easy declarative control of transactions. Unfortunately, it only works on beans managed by Spring inside its context. Salve's transactional instrumentor allows this great feature to be used on any pojo instrumented by Salve independent of whether or not it is declared in the Spring's context.

# Setup #

## Project Setup ##

  * Add salve-depend-spring jar as a runtime dependency
  * Add salve-depend-spring-inst as a build time or runtime dependency based on instrumentation setup
  * Enable instrumentation by adding salve.depend.spring.txn.TransactionalInstrumentor to salve.xml

See the bottom of the page for pom.xml and salve.xml samples

## Spring Integration ##

## Salve 1.2 and Later ##

```
<beans>
  <!-- integrate spring context with dependency library -->
  <bean class="salve.depend.spring.SalveConfigurator"/>

  <!-- integrate transactional instrumentor with txn manager-->
  <bean class="salve.depend.spring.txn.SpringTransactionManager">
    <property name="transactionManager" ref="transactionManager"/>
  </bean>
</beans>	
```


### Short Verson in Salve 1.1 ###

```
<beans>

  <!-- connect salve transactions to spring -->
  <bean class="salve.depend.spring.txn.SpringTransactionManager"/>

  <!-- enable spring's handling of @Transactional to setup the plumbing we will need - this will not interfere with Salve because it removes @Transactional annotation during instrumentation -->
  <tx:annotation-driven>

  <!-- define your required 'transactionManager' here as usual -->
</beans>
```

### Long Version in Salve 1.1 ###
(without using tx:annotation-driven tag)

```
<beans>
  <!-- connect salve transactions to spring -->
  <bean class="salve.depend.spring.txn.SpringTransactionManager"/>

  <!-- set up the plumbing that tx:annotation-driven would set up for us -->

  <!-- configure transaction attributes on annotation @Transactional -->
  <bean id="txAttributeSource" class="org.springframework.transaction.annotation.AnnotationTransactionAttributeSource"/>

  <!-- transaction interceptor for calling transacted methods + types -->
  <bean id="txInterceptor" class="org.springframework.transaction.interceptor.TransactionInterceptor">
    <property name="transactionManager" ref="transactionManager"/>
    <property name="transactionAttributeSource" ref="txAttributeSource"/>
  </bean>

  <!-- scans for @Transactional on methods + types to recommend for instrumentation -->
  <bean class="org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor">
    <property name="transactionInterceptor" ref="txInterceptor"/>
  </bean>

  <!-- define your required 'transactionManager' here as usual -->

</beans>
```


---


### pom.xml sample ###
```
<properties>
  <salve.version>1.1</salve.version>
</propertes>
<dependency>
  <groupId>salve</groupId>
  <artifactId>salve-depend=spring</artifactId>
  <version>${salve.version}</version>
</dependency>
<dependency>
  <groupId>salve</groupId>
  <artifactId>salve-depend-spring-inst</artifactId>
  <version>${salve.version}</version>
  <scope>provided</scope>
</dependency>
```

### salve.xml sample ###
```
<config>
  <package name="com.myproject">
    <instrumentor class="salve.depend.DependencyInstrumentor" />
    <instrumentor class="salve.depend.spring.txn.TransactionalInstrumentor" />
  </package>
</config>
```