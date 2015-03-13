## Prerequisites ##
A valid META-INF/salve.xml file. How to create this file is described in various sections of the wiki, such as the dependency section and the aspects section.

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
java ... -javaagent:~/.m2/repository/salve/salve-agent/2.0-alpha1/salve-agent-2.0-alpha1.jar
```

## Maven2 Build ##
### When ###
Best option when the project is using maven2 for builds and no java command line access is available for deployment.

### How ###
Add the following lines to pom.xml

```
<!-- fragment to demonstrate invocation of the maven plugin -->
<properties>
  <salve.version>2.0-alpha1</salve.version>
</properties>
<dependencies>
  <dependency>
    <!-- salve dependency injection -->
    <groupId>salve</groupId>
    <artifactId>salve-depend</artifactId>
    <version>${salve.version}</version>
   </dependency>
  <dependency>
    <!-- salve dependency instrumentor -->
    <groupId>salve</groupId>
    <artifactId>salve-depend-inst</artifactId>
    <version>${salve.version}</version>
    <!-- no need to include this in final packaging -->
    <scope>provided</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>salve</groupId>
      <artifactId>maven-salve-plugin</artifactId>
      <version>${salve.version}</version>
      <configuration>
        <verbose>true</verbose>
        <debug>true</debug>
      </configuration>
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

## Ant Build ##
Coming soon

## Eclipse ##
Salve has an eclipse builder plugin which can incrementally instrument files as you are editing them. See salve-eclipse module in svn.

Eclipse update site: http://salve.googlecode.com/svn/eclipse-update-site

## IDEA ##
Coming soon - needs to be updated for 2.x