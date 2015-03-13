**This page is a work in progress...**

## Prerequesites ##
**salve-aop** and **salve-aop-inst** jars and their dependencies on the classpath.

### Maven ###
```
<properties>
  <salve.version>2.0-alpha1</salve.version>
</properties>
<dependencies>
  <dependency>
    <groupId>salve</groupId>
    <artifactId>salve-aop</artifactId>
    <version>${salve.version}</version>
   </dependency>
   <dependency>
    <groupId>salve</groupId>
    <artifactId>salve-aop-inst</artifactId>
    <version>${salve.version}</version>
  </dependency>
</dependencies>
```

## Building the Advice ##

Advices in Salve are kept in public static methods of the following form:

```
public static class StringAdvices
{
  public static Object uppercase(salve.aop.MethodInvocation invocation) throws Throwable
  {
     String value=(String)invocation.execute();
     return (value!=null)?value.toUpperCase():null;
  }
}
```

## Defining the PointCut ##

Pointcuts are defined by annotations. Annotations can be applied to either a method or any of the method's arguments.

```
@Retention(RetentionPolicy.RUNTIME)
@MethodAdvice(adviceClass = StringAdvices.class, adviceMethod = "uppercase")
public @interface Uppercase {}
```

## Applying the Advice ##
```
public class Service
{
  @Uppercase
  public String echo(String str) { return str; }
}
```

## Configuration ##
You need a small configuration file telling salve which instrumentors to run. This file is kept in META-INF/salve.xml, and usually looks like this:

```
<config>
  <packages>
    <package>
      <name>com.mycompany.myproject</name>
      <instrumentors>
        <salve.aop.inst.AopInstrumentor>
      </instrumentors>
    </package>
  </packages>
</config>
```

Replace <com.mycompany.myproject> with the base package name of your application. The `<packages>` element can contain multiple `<package>` elements.

## Instrumentation ##

See the [Instrumentation Setup](InstrumentationSetup2.md) page for instructions on how to setup bytecode instrumentation.