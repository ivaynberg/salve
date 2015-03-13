A lot of people ask me why Salve comes with its own instrumentors and is not built on top of an aspect library like AspectJ. The answer is simple, I have not found an aspect library that would let me do what I want. The philosophy of AspectJ is to produce binary compatible code, but Salve requires a lot of destructive changes to class structure.

### Removing Fields ###
Salve removes fields from classes. There are many reasons to do this, but to name the major ones:
  * It saves on memory
  * When using persistence libraries there is no need to add special annotations onto the field, such as @Transient - Yes, I can use AspectJ to introduce the @Transient annotation, but then I have to have the jar containing that annotation on the classpath...something that the JVM itself does not require and something I can easily accomplish myself when using ASM.
  * No serialization problems because the field is not there - Yes, AspectJ can be used to introduce the transient keyword, but it is still much easier if it is not there.

### Removing Annotations ###
Salve removes annotations and swaps them out for other ones. For example it replaces `org.springframework.transaction.annotation.Transactional
` with its own analog to avoid double instrumentation by Spring. AspectJ does not allow this.

### Pointcut Expressiveness ###
When I started working on Salve I considered using AspectJ. At that time, however, it did not support pointcuts matching on method argument annotations. I always wanted to implement @NotNull as part of Salve because I hate writing the four to five if-else blocks in the beginning of each method, and even more so, I hate reading them. Two years later AspectJ still has very limited support for this, eg see this [blog](http://andrewclement.blogspot.com/2009/02/aspectj-advising-methods-with-parameter.html) for how to implement @NotNull using latest AspectJ.

### Efficiency ###
I do not think this would play a huge factor, but ... Salve creates a lot of private static variables on the class that it uses to cache frequently used data, such as dependency keys. Because I know what fields I generate during bytecode instrumentation phase I do not need reflection to access them and I can create as many fields as I want.

### Handling Synthetic Accessor Methods ###
Suppose
```
Class A {
  @Dependency private Printer printer;
  
  private class B {
     public void execute() {
       printer.print();
     }
  }
}
```

This construct is supported by Salve because Salve instruments synthetic accessor methods generated by the compiler, while AspectJ does not.
See [here](http://www.nabble.com/Annotations,-inner-classes-and-set----withincode-td24080336.html) for a more thorough explanation.

### The Future ###
I keep an eye out on AspectJ because it is a very cool project and it comes with an awesome Eclipse plugin. I think as soon as it allows me to do some of the things mentioned above or I think it is worth giving some things up I will rewrite Salve on top of AspectJ.