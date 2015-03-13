Getting started with Salve is simple:

### Step 1: Integrate DependencyLibrary with your IOC container ###
When Salve needs to look up a dependency it does so by looking through all Locators in the DependencyLibrary singleton. To integrate Salve with your project you simply have to install a Locator that looks up dependencies from your IOC container of choice. Out of the box Salve ships with locators for Spring and Guice. Building a custom locator to integrate with other IOC containers is trivial. For more information on how to configure the DependencyLibrary please see [here](ConfiguringDependencyLibrary.md).

### Step 2: Setup code instrumentation ###
Salve works by changing the bytecode of classes that use Salve annotations. All three major instrumentation paradigms are supported: load-time weaving, build-time instrumentation, and as-you-go IDE integrations. For details please see [here](ConfiguringInstrumentation.md)

### Step 3: Using Salve ###
This is the simplest step of all, simply place Salve annotations on your classes :)

```
public class Account {
  @Dependency private EmailSender sender;

  public void debit(BigInteger amount) {
    sender.send(ower, "Debited account: "+id+" for amount: "+amount);
  }
}
```