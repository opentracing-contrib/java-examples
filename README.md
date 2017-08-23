[![Build Status][ci-img]][ci]

# java-examples
tester examples of common instrumentation patterns


[ci-img]: https://travis-ci.org/opentracing-contrib/java-examples.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-examples

## Testing against a Java Opentracing  PR/branch.

In order to test these examples against https://github.com/bhs/opentracing-java/pull/5, you have to install it first in the local Maven repository:

    $ cd opentracing_java/
    $ git remote add bhs https://github.com/bhs/opentracing-java
    $ git fetch bhs pull/5/head:bhs/scopes
    $ git checkout bhs/scopes
    $ mvn compile install

By default the version for a build looks like `0.30.1-SNAPSHOT`, which helps to keep the stable and the testing one side by side.

Next, look for the `<properties>` section in the `pom.xml` file under this directory, updating the `opentracing` version; and also add a dependency to the `usecases` package:

```xml
<opentracing.version>0.30.1-SNAPSHOT</opentracing.version>
...
    <dependency>
      <groupId>io.opentracing</groupId>
      <artifactId>opentracing-usecases</artifactId>
      <version>${opentracing.version}</version>
      <scope>test</scope>
    </dependency>
```

Finally, run (under this directory as well):

    $ mvn test
