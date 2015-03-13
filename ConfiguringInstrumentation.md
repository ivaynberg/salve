## Prerequisites ##
Make sure you have salve jars.
See [BuildingSalve](BuildingSalve.md) page for instructions on how to build Salve from source.

You need a small configuration file telling salve which instrumentors to run. This file is kept in META-INF/salve.xml, and usually looks like this:

```
<config>
	<package name="<base.package>">
		<instrumentor class="salve.depend.DependencyInstrumentor"/>
	</package>
</config>
```

Replace <base.package> with the base package name of your application. For example: `com.mycomp.myapp`. The `<config>` element can contain multiple `<package>` elements.

## JVM Agent ##
### When ###
Best option for development as no special code post-compilation step is needed, making launching the application directly from an IDE easy. Can also be used at deployment time if access to java command line is available.

### How ###
To enable the agent add the following JVM option to the startup command:
```
-javaagent:<path to salve-agent.jar>
```

Example:
```
java ... -javaagent:~/.m2/repository/salve/salve-agent/1.0/salve-agent-1.0.jar
```

## Maven2 Build ##
### When ###
Best option when the project is using maven2 for builds and no java command line access is available for deployment.

### How ###
Add the following lines to pom.xml

```
<!-- fragment to demonstrate invocation of the maven plugin -->
<dependencies>
  <dependency>
    <!-- salve dependency injection -->
    <groupId>salve</groupId>
    <artifactId>salve-depend</artifactId>
    <version>1.1</version>
   </dependency>
  <dependency>
    <!-- salve dependency instrumentor -->
    <groupId>salve</groupId>
    <artifactId>salve-depend-inst</artifactId>
    <version>1.1</version>
    <!-- no need to include this in final packaging -->
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>salve</groupId>
      <artifactId>maven-salve-plugin</artifactId>
      <version>1.1</version>
      <!--<configuration>-->
        <!-- print the instrumented classes on the console -->
        <!--<verbose>true</verbose>-->
        <!-- implies verbose=true and additionallys show which files are scanned for instrumentation -->
        <!--<debug>true</debug>-->
      <!--</configuration>-->
      <executions>
        <execution>
          <goals>
            <!-- instrument production classes in target/classes -->
            <goal>instrument</goal>
            <!-- instrument test classes in target/test-classes -->
            <goal>instrument-test</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

In legacy versions before 1.1 the configuration was slightly different:

```
<dependencies>
  <dependency>
    <!-- salve dependency injection -->
    <groupId>salve</groupId>
    <artifactId>salve-depend</artifactId>
    <version>1.0</version>
   </dependency>
  <dependency>
    <!-- salve dependency instrumentor -->
    <groupId>salve</groupId>
    <artifactId>salve-depend-inst</artifactId>
    <version>1.0</version>
    <!-- no need to include this in final packaging -->
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>salve</groupId>
      <artifactId>salve-maven2</artifactId>
      <version>1.0</version>
      <executions>
        <execution>
          <phase>process-classes</phase>
          <goals>
          <goal>instrument</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```



## Ant Build ##
Coming soon

## Eclipse ##
Salve has an eclipse builder plugin which can incrementally instrument files as you are editing them. See salve-eclipse module in svn.

Eclipse update site: http://salve.googlecode.com/svn/eclipse-update-site

## IDEA ##
There's an plugin for IDEA, too...

The plugin will scan for META-INF/salve.xml in your source directories and instrument your class files automatically when IDEA compiles them. The plugin can be found under the name 'Salve Integration' in IDEA's 'Available' Tab in the 'Plugins' settings dialog easily.

Link to the plugin on IntelliJ.net: http://plugins.intellij.net/plugin/?id=3709