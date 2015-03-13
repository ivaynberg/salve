# Introduction #

One of the best features of Spring introduced in 2.0 is the @Transactional annotation which allows an easy declarative control of transactions. Unfortunately, it only works on beans managed by Spring inside its context. Salve's transactional instrumentor allows this great feature to be used on any pojo instrumented by Salve independent of whether or not it is declared in the Spring's context.

# Setup #

## Overview ##

  * Add salve-depend-spring jar as a runtime dependency
  * Add salve-depend-spring-inst as a build time or runtime dependency based on instrumentation setup
  * Enable instrumentation by adding salve.depend.spring.txn.TransactionalAdviceAdapter to aop instrumentor config in salve.xml

## Jars with Maven ##

```
<properties>
  <salve.version>2.0-alpha1</salve.version>
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

## Spring Integration ##

```
<beans>
  <!-- integrate spring context with dependency library -->
  <bean class="salve.depend.spring.SalveConfigurator" />

  <!-- integrate transactional instrumentor with txn manager-->
  <bean class="salve.depend.spring.txn.SpringTransactionManager">
    <property name="transactionManager" ref="data.transactionManager" />
  </bean>
</beans>	
```

## Configuration in salve.xml ##
```
<config>
  <packages>
    <package>
      <name>com.mycompany.myproject</name>
      <instrumentors>
        <salve.aop.inst.AopInstrumentor>
          <aspectProviders>
            <salve.depend.spring.txn.TransactionalAspectProvider/>
           </aspectProviders>
        </salve.aop.inst.AopInstrumentor>
      </instrumentors>
    </package>
  </packages>
</config>
```