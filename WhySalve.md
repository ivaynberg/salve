Salve aims to fix the anemic domain model anti-pattern([1](http://www.martinfowler.com/bliki/AnemicDomainModel.html)) ([2](http://www.theserverside.com/patterns/thread.tss?thread_id=31010#172016)) present in most modern java web-applications.

With technologies like ORM we are able to model domain layer objects that contain application data, but we are unable to enrich them with behavior to act upon that data - the basic principle of OOP. The behavior is factored out into stateless singleton objects called services. This is done for the following reasons:
  * **Dependency Management**: Objects modeled by the domain layer depend on services provided by other parts of the application, such as jms/email/orm/database. It is much easier to provide these dependencies to an object whose lifecycle is managed by an IOC container rather then to one whose lifecycle is handled by the ORM framework or by the user using the `new` operator.
  * **Instance Weight**: Domain layer objects need to be lightweight because they are often passed around between application layers/cluster nodes, and sometimes stored by the user for later reference. This often entails having the ability to be serializable which means these objects cannot carry references to heavy objects that would significantly increase their serialized-size, or carry references to objects that are not themselves serializable or should not be serialized - such as singletons.
  * **Testability**: The two concerns above can be addressed by allowing objects to lookup their dependencies inside methods (rather then keeping them as fields) via some sort of a static lookup. However this makes it more difficult to test the object because its dependencies are harder to discover and mocks are harder to pass in - compared to objects that use setter methods to set their dependencies.

### How Salve Fixes the Problem ###
The ideal solution would allow domain layer objects to be lightweight, contain business logic, and have dependency injection that works under any lifecycle. One way to achieve this would be to allow the developer to specify dependencies as fields, and then rewrite the bytecode to remove those fields from the class and replacing access to them via a static lookup into whatever IOC container/dependency management system the application is using when the application is deployed. This is how Salve works. Lets take a look at a quick example:

```
// This is a JPA entity
@javax.persistence.Entity public class Account {

  // this is a dependency we wish Salve to instrument
  // notice no need for @Transient because the field will be removed
  @salve.Dependency
  private EmailService email;

  // this is database id of this account
  @javax.persistence.Id
  private Long id;

  // this is a JPA field
  private float balance;

  // setters and getters

  public void withdraw(float amount) {
    if (amount>balance) {
      email.sendOverdrawMessage(this);
    }
    balance-=amount;
  }
}
```

Without Salve such an object is not practical: there is no good way to set the `EmailService` dependency, and there is no good way to have `Account` be serializable.

When the bytecode of Account class is processed by Salve, it will look like this:
```
@javax.persistence.Entity public class Account {

  // added by salve to facilitate lookup of email service dependency
  private static final salve.depend.Key _salve_key$email=new salve.depend.Key(...);

  // email field is removed

  @javax.persistence.Id
  private Long id;

  private float balance;

  // setters and getters

  public void withdraw(float amount) {
    // dependencies are looked up lazily and cached in a local var
    // there is also a cache in salve that speeds up multiple repated lookups
    EmailService _email=salve.depend.DependencyLibrary.lookup(_salve_key$email);

    if (amount>balance) {
      _email.sendOverdrawMessage(this);
    }
    balance-=amount;
  }
}
```

Lets take a look at the changes Salve made to the code:
  * `EmailService email` field is removed - removing a reference to a heavy service object and making `Account` lightweight
  * Access to the `email` field is replaced with a static lookup into a method-local variable. For instructions on configuring Salve's ConfiguringDependencyLibrary lookups see [ConfiguringDependencyLibrary](ConfiguringDependencyLibrary.md) page.

For instructions on how to setup Salve in your project see the GettingStarted page