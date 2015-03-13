Salve allows creation of rich domain models by removing limitations that cause anemic domain model anti-pattern ([1](http://www.martinfowler.com/bliki/AnemicDomainModel.html)) ([2](http://www.theserverside.com/patterns/thread.tss?thread_id=31010#172016)) and procedural-style code. Salve allows POJOs to be injected with heavy singleton dependencies yet remain lightweight, serializable, and have unlimited lifecycle (such as being instantiated via the `new` operator). Salve accomplishes this by instrumenting bytecode either at load time via a JVM agent or at build time via a post-compilation step. For more information see [WhySalve](WhySalve.md) wiki page.


### News ###
  * 20091022 Salve 2.0 Released
    * This is the final release of the 2.0 version of Salve. No issues were found in Beta1 so this release is equivalent but with an adjusted version number.
    * Trunk will be switching to Salve 3.0 which will be AspectJ based.

  * 20090930 Salve 2.0 Beta1 Released
    * Changelog
      * [Issue 18](http://code.google.com/p/salve/issues/detail?id=18): Eclipse plugin should not instrument resources marked in error
      * [Issue 19](http://code.google.com/p/salve/issues/detail?id=19): Add ability for instrumentors to report errors and warnings
      * [Issue 21](http://code.google.com/p/salve/issues/detail?id=21): Can @Transactional be on a static method?
      * [Issue 22](http://code.google.com/p/salve/issues/detail?id=22): Use a different filename for salve 2 config file (suggestion: salve2.xml)
      * [Issue 23](http://code.google.com/p/salve/issues/detail?id=23): Create logging infrastructure
    * Maven repository: [http://salve.googlecode.com/svn/maven2](http://salve.googlecode.com/svn/maven2)
    * Svn url: [http://salve.googlecode.com/svn/tags/releases/salve-2.0-beta1](http://salve.googlecode.com/svn/tags/releases/salve-2.0-beta1)
    * Eclipse update site: [http://salve.googlecode.com/svn/eclipse-update-site](http://salve.googlecode.com/svn/eclipse-update-site)

  * 20090917 Salve 2.0 Alpha1 Released
    * Salve now comes with its own basic AOP framework that can be used to [easily create reusable aspects](CreatingAspects2.md).
    * Changelog
      * [Issue 13](http://code.google.com/p/salve/issues/detail?id=13&can=1&q=label%3AFixed-In-2.0-alpha1): Implement a basic aop framework
      * [Issue 17](http://code.google.com/p/salve/issues/detail?id=17&can=1&q=label%3AFixed-In-2.0-alpha1): Allow @Dependency to work on static fields
      * [Issue 20](http://code.google.com/p/salve/issues/detail?id=20&can=1&q=label%3AFixed-In-2.0-alpha1): Simplify Spring transactional integration
    * Maven repository: [http://salve.googlecode.com/svn/maven2](http://salve.googlecode.com/svn/maven2)
    * Svn url: [http://salve.googlecode.com/svn/tags/releases/salve-2.0-alpha1](http://salve.googlecode.com/svn/tags/releases/salve-2.0-alpha1)
    * Eclipse update site: [http://salve.googlecode.com/svn/eclipse-update-site](http://salve.googlecode.com/svn/eclipse-update-site)

  * 20090614 Salve 1.1 is released
    * Changelog
      * [Issue 9](http://code.google.com/p/salve/issues/detail?id=9): Improve dependency resolution conflict handling
      * [Issue 10](http://code.google.com/p/salve/issues/detail?id=10): Salve's @Transactional does not work with TestNG's @Test declared on methods
      * [Issue 12](http://code.google.com/p/salve/issues/detail?id=12): Salve should propagate additional @annotations next to @Transactional in lookup key to allow lookup of different transaction managers
    * Maven repository: [http://salve.googlecode.com/svn/maven2](http://salve.googlecode.com/svn/maven2)
    * Svn url: [http://salve.googlecode.com/svn/tags/releases/1.1](http://salve.googlecode.com/svn/tags/releases/1.1)
    * Eclipse update site: [http://salve.googlecode.com/svn/eclipse-update-site](http://salve.googlecode.com/svn/eclipse-update-site)