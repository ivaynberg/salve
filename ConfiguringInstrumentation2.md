## Prerequisites ##
Make sure you have salve jars.
See [BuildingSalve](BuildingSalve.md) page for instructions on how to build Salve from source.

## Config file ##
You need a small configuration file telling salve which instrumentors to run. This file is kept in META-INF/salve.xml, and usually looks like this:

```
<config>
  <packages>
    <package>
      <name>com.mycompany.myproject</name>
      <instrumentors>
        <salve.depend.DependencyInstrumentor/>
      </instrumentors>
    </package>
  </packages>
</config>
```

Replace <com.mycompany.myproject> with the base package name of your application. The `<packages>` element can contain multiple `<package>` elements.

## Instrumenting Bytecode ##

See the [Instrumentation Setup](InstrumentationSetup2.md) page for instructions on how to setup bytecode instrumentation.