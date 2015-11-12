Domains Service Template
=====

This is a dropwizard based service template to use for spinning up new projects.  

What this project gives you:

- Web enabled health checks and monitoring (via port 8081)
- Properly set up SOA service modules
- Version tagging (branch and git SHA) as part of all jar manifests
- Version information in health checks
- Unit testing and code coverage set ups
- Java 1.8
- Docker packaging support using the java base image

This project is ideal for both web services and for long running queue listeners. Even though web ports are specified, you get the utiltiies of a web based administration console in a long running process.

Required
====

RPM program. Install via brew or cygwin.

Running your docker image

Execute your service
====

```
./scripts/publish-docker.sh --build --package --name cassandra-queue-service --tag dev

./scripts/run-core.sh
```

Known issues
====

If you get an error like:


```[ERROR] Failed to execute goal org.apache.maven.plugins:maven-shade-plugin:2.2:shade (default) on project test-service-core: Error creating shaded jar: java.util.jar.Attributes cannot be cast to java.lang.String -> [Help 1]```

It's because we are tryign to include the git branch and sha in the manifest but you are building the repo outside of a git project. Do a

```
git init
git add --all .; cm "Initial checkin";
```

And retry
