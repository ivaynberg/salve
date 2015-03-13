### Introduction ###

Salve has very basic design by contract support that is designed to remove some noisy but necessary code.

For example:

```
class MyArray {
  /** delete object located somewhere between two indices */
  public void deleteBetween(Object obj, int start, int end) {
    if (obj==null) {
      throw new IllegalArgumentException("Argument obj cannot be null");
    }
    if (start<0) {
      throw new IllegalArgumentException("Argument start cannot be less then zero");
    }
    if (end<0) {
      throw new IllegalArgumentException("Argument end cannot be less then zero");
    }
    if (end<start) {
       throw new IllegalStateException("Argument end cannot be less then argument start");
    }
    for (int i=start;i<=end;i++) { .... }
}
```

The first nine lines of the example are noisy, but they are necessary so we can fail early with a message that is meaningful to the user.

With Salve's contract module the above can be rewritten as:

```
class MyArray {
  /** delete object located somewhere between two indices */
  public void deleteBetween(@NotNull Object obj, @GE0 int start, @GE0 int end) {
    if (end<start) {
       throw new IllegalStateException("Argument end cannot be less then argument start");
    }
    for (int i=start;i<=end;i++) { .... }
}
```

As you can see, we were able to remove some noise from the code.

### Available Annotations ###
|annotation|target|description|
|:---------|:-----|:----------|
|@NotNull|method or method arguments|checks whether or not the specified argument or return value is null, throws IllegalArgumentException for arguments and IllegalStateException for method return value|
|@NotEmpty|String method or method arguments|Implies NotNull, additionally performs isEmpty() check|
|@GT0|Numeric method or method arguments|Checks whether or not the return value or argument is greater then zero|
|@GE0|Numeric method or method arguments|Checks whether or not the return value or argument is greater then or equal to zero|
|@LT0|Numeric method or method arguments|Checks whether or not the return value or argument is less then zero|
|@LE0|Numeric method or method arguments|Checks whether or not the return value or argument is greater then or equal to zero|
|@InitOnce|Field|Annotated fields can only have their values set to a non-null value once. Useful when a public setter is needed but the value can only be set once.|

### Installation ###
Salve provides two contract modules `salve-contract` and `salve-contract-inst`. The former contains runtime dependencies like the annotations, and the latter bytecode instrumentors. If you are using maven2 your dependencies will look like this:

```
<properties>
  <salve.version>1.1</salve.version>
</propertes>
<dependency>
  <groupId>salve</groupId>
  <artifactId>salve-contract</artifactId>
  <version>${salve.version}</version>
</dependency>
<dependency>
  <groupId>salve</groupId>
  <artifactId>salve-contract-inst</artifactId>
  <version>${salve.version}</version>
  <scope>provided</scope>
</dependency>
```

Notice `provided` scope on the salve-contract-inst module. Since we will be performing bytecode instrumentation during the build process using maven2 there is no need for that dependency at runtime.

Salve configuration example:

```
<config>
  <package name="salve.blog">
    <instrumentor class="salve.contract.ContractInstrumentor" />
  </package>
</config>
```