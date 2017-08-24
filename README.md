[![Build Status][ci-img]][ci]

# java-examples
tester examples of common instrumentation patterns


[ci-img]: https://travis-ci.org/opentracing-contrib/java-examples.svg?branch=master
[ci]: https://travis-ci.org/opentracing-contrib/java-examples

## Testing against a Java Opentracing  PR/branch.

In order to test these examples against an `opentracing-java` branch, you have to install it in local Maven repository:

    $ cd opentracing_java/
    $ git checkout your-opentracing-java-branch
    $ mvn compile install

If you need to fetch a branch from somebody else's repository, you need to fetch it as a local branch first:

    $ git remote add another-user https://github.com/another-user/opentracing-java/
    $ git fetch another-user
    $ git checkout -b testing-branch another-user/testing-branch

By default the version for a build looks like `0.30.1-SNAPSHOT`, which helps to keep the stable and the testing one side by side.

Next, look for the `<properties>` section in `java-examples/pom.xml`, updating the `opentracing` version:

```xml
<opentracing.version>0.30.1-SNAPSHOT</opentracing.version>
```

Finally, run under `java-examples`:

    $ mvn test

Any further changes under `java-examples`  will not need re-installing anything, simply re-running `mvn test` (as shown in the last step).
