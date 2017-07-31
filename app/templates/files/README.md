# Spring Boot Reference Application

The intention of the Reference Application is to serve as a guide when developing Business Applications
(Fagapplikasjoner) within The Norwegian Tax Administration (Skatteetaten); applications implementing the core business 
rules in the tax domain for which Skatteetaten is responsible.

In this repository your will find examples on how to solve common technical issues and implement requirements for 
applications running within the networks of Skatteetaten and especially on the 
[Aurora OpenShift platform](https://aurora/wiki/display/OS/OPENSHIFT). This includes logging, handling database 
migrations, testing, security, application versioning, build pipeline, to name a few.

The Reference Application is implemented in [Spring Boot](https://projects.spring.io/spring-boot/).


# About the Core Technologies

The recommended technology for all new business applications created within Skatteetaten is Spring Boot. Recommending
a technology in this way is always associated with some controversy, especially in an organization with history of using
several other technology stacks. It is, however, our opinion that at the time there are few other stacks that are better
supported and are driving development in the Cloud Native space better than Spring Boot.

For more information on the decision process around selecting Spring Boot, see [reference missing].


# The Aurora Requirements

The non-functional requirements for application running on the Aurora Openshift platform is documented here:
[Krav til applikasjoner som skal kjøre på OpenShift](https://aurora/wiki/pages/viewpage.action?pageId=108362986).


# How to Use the Application

As the intention of the Reference Application is to serve as a guide to how to set up your own application, it is not 
intended to be used directly as a starting point for new applications without any modifications.

You may fork the repository of the application to be able to apply new commits onto your own modified application, or
your may export the code into your own fresh repository and manually keep up to date with changes to the central
repository by inspecting the change logs. Which approach is best will depend on the amount of changes you make to 
central files, like the ```pom.xml``` and ```application.yml```.

Regardless of the approach you use to keep up with changes, you will have to make the following changes to the
fork/export:

 * remove the openshift folder, it is there to be used for demos. If you want you can use the aoc configuration as inspiration when you create your own aoc configuration in a seperate repository
 * change the ```groupId``` and ```artifactId``` in the ```pom.xml``` to match that of your application
 * rename the main package in ```src/main``` to match that of your application
 * change the ```info.application.name``` property in ```application.yml``` to match that of your application
 * change the ```info.links``` in ```application.yml``` to match that of your application
 * remove the example database code (migrations under ```src/main/resource/db/migrations```), the Counter-classes in the controllers, health and service packages and the database config in ```application.yml```


# What is Covered in the Application?

## Starters

The application has one starter  [aurora-spring-boot-starter](https://git.aurora.skead.no/projects/AUF/repos/aurora-spring-boot-starters/browse/aurora) in order to set up normal aurora requirements such as
 - grouping properties into their own property sources
 - setting default properties for actuator
 - instrumenting RestTemplates with metrics
 - instrument ServerFilter with metrics
 - instrument logback with metrics
 
There is a starter for testing with spock [aurora-spock-spring-boot-starter](https://git.aurora.skead.no/projects/AUF/repos/aurora-spring-boot-starters/browse/aurora-oracle)

A starter for working with oracle databases with flyway [aurora-oraclespring-boot-starter](https://git.aurora.skead.no/projects/AUF/repos/aurora-spring-boot-starters/browse/aurora-spock)

In order to auto configure a datasource provided on openshift add the following configuration to your properties file
 
    aurora:
      db: NAME_OF_DB
       
All starters are versioned together.

## Log Configuration


All Skatteetaten applications should log using the same standard logging pattern. Also, all applications running
on Openshift has to log to a specific folder in the container for the logs to picked up and automatically indexed in
Splunk. Logfiles put in the container need to be rolled before reaching a predetermined size to avoid the platform
terminating the application (in self defence; log files filling up disk on the Openshift nodes may wreck havoc in a 
cluster).

To avoid every application having to maintain their own ```logback.xml```-file implementing all the requirements, one
is provided by the platform in the runtime Docker image. The reference application is configured to use this Logback
file when it exists, while still preserving the convenient Spring Boot features for setting the log level. This is done
in the ```src/main/assembly/metadata/openshift.json```-file. The ```application.yml```-file also contains an example on
how to set the log levels.

For details, see:
* [OPENSHIFT: Hvordan få riktig konfigurert logging](https://aurora/wiki/pages/viewpage.action?pageId=121982307)
* [Logging og feilhåndtering - Krav](https://aurora/wiki/pages/viewpage.action?pageId=34578383)


## HTTP Header Handling

According to the Aurora Requirements all applications should use the ```Klientid``` header for identifying themeselves
when they operate as a client to other services. They should also send a ```Korrelasjonsid``` header for traceability.
They may also optionally send a ```Meldingsid``` header as an identifier for the current message.

The value of the ```Klientid```, ```Korrelasjonsid``` and ```Meldingsid``` headers should be logged on every request.
This is implemented by using a filter that will extract the values of these headers and putting them on 
[SLF4J MDC](http://www.slf4j.org/api/org/slf4j/MDC.html). The artifact implementing the filter is 
[aurora-header-mdc-filter](https://aurora/git/projects/AUF/repos/aurora-header-mdc-filter/browse) and is included
as a dependency in the pom.xml-file. See the ```ApplicationConfig```-class for details on how the filter is configured.

For details, see:
* [Logging og feilhåndtering - Krav](https://aurora/wiki/pages/viewpage.action?pageId=34578383)
* [UT-AUR-LOG-006](https://aurora/wiki/display/AURORA/UT-AUR-LOG-006)
* [UT-AUR-REST-004](https://aurora/wiki/display/AURORA/UT-AUR-REST-004)

 
## Database Migrations with Flyway

Databases are migrated using the database migration tool [Flyway](https://flywaydb.org/). Migrations should be put in
the ```src/main/resources/db/migration```-folder. Flyway is included as a dependency in the pom.xml-file and Spring
Boot automatically picks it up and configures it using the application DataSource.

Flyways out-of-order mode is enabled by default which allows migrations to be created in branches with concurrent
development activity and hot fixes without running the risk of creating a series of migrations that
cannot be applied because they have been created with an index/timestamp older than migrations already run against
the database.

The recommended naming scheme for migration files is ```VyyyyMMddHHmm__Migration_short_description.sql```, for
example ```V201609232312__CounterTableInit.sql```. Using
timestamps in preference of indexes is recommended to avoid having to coordinate migration indexes across branches
and developers.

For details, see:
* [Flyway SpringBoot](https://flywaydb.org/documentation/plugins/springboot)
* [Spring Doc: Use a higher-level database migration tool](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html#howto-execute-flyway-database-migrations-on-startup)


## HTTP Endpoint JSON serialization

The format for Dates in JSON responses has been set to com.fasterxml.jackson.databind.util.ISO8601DateFormat in
preference of the default format which is just milli seconds since Epoc.


## Management interface

The management interface is exposed on a seperate port (MANAGEMENT_HTTP_PORT) for security reasons. The main part of
this is Spring Boot Actuator (with HATEOAS). Most of the endpoints are tured off by default. See the ```resources/application.yml``` file for more details. 

A central component Marjory is deployed in the openshift cluster that aggregate the management interfaces for all 
applications and works as a proxy to the hidden management interface. 

The standard management interface consist of the following urls

###  /info - Information 

The /info endpoint is particularly relevant because it is used by Marjoy  to collect and display information about the application. 
Some of that information is maintained and set in the ```application.yml``` file
(everything under ```info.*``` is exposed as properties), and some is set via maven plugins;
  
  * the spring-boot-maven plugin has a build-info goal that is configured to run by default. This will goal will create
  a file ```META-INF/build-info.properties``` which includes  information like build time, groupId and artifactId. This
  will be added to the info section by spring actuator. 
  See [Spring Boot Maven Plugin](http://docs.spring.io/spring-boot/docs/current/maven-plugin/examples/build-info.html)
  for details.
  * the git-commit-id-plugin will create a file ```git.properties``` which includes information like committer, 
  commit id, tags, commit time etc from the commit currently being built. Spring actuator will add this information to
  the info section.

The info endpoint has a dependencies section that list the name of all the external dependices and their static base URL. 
This information will be stored in a CMDB for cause analysis and to chart dependencies in Skatteetatens infrastructure.

The links part of the info endpoint show application specific links to other part of the internal infrastructure in Skatteeaten. 
The links contains some placeholders that are replaced by Marjory marked with ```{}``` fences.

###  /health - Health status 

The health endpoint is used to communicate to the platform that your application is ready to receive traffic. You
should add your own custom application specific health checks to make sure this endpoint properly reflects the actual
health state of your appliction. See 
[Hvordan styre når din applikasjon får HTTP trafikk](https://aurora/wiki/pages/viewpage.action?pageId=112138285) for 
more details.

We use an status OBSERVE in HealthChecks on order for the application to send OBSERVE status to Aurora Console


For more info see:
* [Spring Doc: Health information](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html#production-ready-health)


###  /env - Configuration 
This endpoint prints out the given configuration for an application. Sensitive fields are masked using standard Spring Boot
mechanics. 

### /prometheus - Metrics   

This reference application uses a library [aurora-prometheus](https://git.aurora.skead.no/projects/AUF/repos/aurora-prometheus)
in order to directly instrument Prometheus metrics. 

The following metrics are set up automatically
 - http_server_requests histogram for inncomming requestes. 
 - http_client_requests histogram for outgoing requests.
 - operations histogram for manual operations with the ```withMetrics``` method.
 
It is possible to configure the grouping and filtering of both server and client metrics in your application.yaml file. 

    aurora:
        client:
           metricsPathLabelGroupings:
              httpbin: ".*httpbin.org.*"

This configuration says that all outgoing requests that match the regex on the right should be grouped under the 
path label on the left.

    aurora:
        client:
           mode : INCLUDE_MAPPINGS
           metricsPathLabelGroupings:
              httpbin: ".*httpbin.org.*"

Group as above but also exclude everything not matching the regex in the labelGroupings.


    aurora:
        client:
           mode: INCLUDE
           includes:
              httpbin: ".*httpbin.org.*"

Only include the metrics matching the regex on the right

    aurora:
        client:
           mode: EXCLUDE
           includes:
              httpbin: ".*httpbin.org.*"

Exclude the metrics matching the regex on the right

For applications that are deployed to OpenShift, metrics exposed at ```/prometheus``` (default, configurable) in the
format required by Prometheus will be automatically scraped, registered, and become available in the
[central Graphana](https://metrics.skead.no/) instance. 

For more details, see
 * [Hvordan samle inn og se metrikker](https://aurora/wiki/display/OS/Hvordan+samle+inn+og+se+metrikker)

## Security

Management interface is exposed on a seperate port that is not accessable outside of the cluster. This means that no 
information about metrics, env vars, health status is available to the the outside world. 

Tomcat is the application server used in the standard spring boot starter web, it by default disables the TRACE endpoint 
closing another security issue.

All application calls between applications are secured with an application level token that is provided by a central authority.


## Unit Testing with Spock

Unit testing has been set up and configured to use the Spock Framework with support for mocking classes and classes
with non-default constructors. The gmavenplus-plugin and surefire-plugin are configured so that the tests will run in
a standard maven build.

For details, see:
* [gmavenplus-plugin](http://groovy.github.io/GMavenPlus/)
* [Spock Framework](http://spockframework.org/)


## Documentation

For documentation, the Reference Application configured to use [spring-rest-docs](https://projects.spring.io/spring-restdocs/) 
which is an approach to documentation that combines hand-written documentation with auto-generated snippets produced 
with Spring MVC Test. Please read the spring-rest-docs documentation for an overview of how the technology works.

The ```pom.xml``` is set up with the necessary plugins to build the documentation. This is basically including
spring-rest-docs on the test classpath to generate snippets and configuring the asciidoctor-maven-plugin to process
documentation in the ```src/main/asciidoc```-folder and the output folder of the spring-rest-docs tests. The 
maven-resources-plugin is configured to include the generated documentation in ```/static/docs``` in the final jar
which results in spring-boot exposing the files over HTTP on ```/docs``` (see [Static Content](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-web-applications.html#boot-features-spring-mvc-static-content)
from the spring-boot documentation for details).

A class, ```AbstractControllerTest```, is included as an example base class for tests that use spring-rest-docs.

# How an application is built

## Application Configuration and Spring Profiles

The Reference Application is set up to work with two Spring configuration profiles (see [missing link]()); one called
```local``` and one called ```openshift```. The ```local``` profile is active by default (see the 
```spring.profiles.active``` property in ```application.yml```), and the intention is that this profile should be used
when running the application locally during development, and the application should ideally be startable from the IDE
without modifications after cloning it from Git.

In the ```metadata/openshift.json```-file the ```jvmOpts``` property is set to ```-Dspring.profiles.active=openshift```,
which will disable the local profile and activate a profile called ```openshift``` (it could be called pretty much
whatever, as long as it is something different from local). This allows you to have configuration that is active only
when you develop locally, or only when you run the application from the Docker image (for instance on Openshift).

Obviously, this dual profile setup does not help if you need different configuration for different deployed instances of
your application (for instance different environment/namespaces). Openshift supports many methods for applying 
configuration to Docker containers, and Aurora Openshift in particular has guidelines to how environment specific 
configuration should be done.

 * [Hvordan få miljøspesifikke variabler inn i et miljø](https://aurora/wiki/pages/viewpage.action?pageId=112136703)
 * [Hvordan få hemmelige data i en app](https://aurora/wiki/pages/viewpage.action?pageId=112138166)
 * [Min første app med AOC](https://aurora/wiki/pages/viewpage.action?pageId=115402890)


## Build Configuration

This section relates to how the ```pom.xml``` file is set up to produce artifacts and reports. Some of this have already
been covered in detail in other sections and will hence not be covered here again. In fact, most of the features of
the Reference Application have one or more entires in the pom. This section covers what is not explicitly covered 
elsewhere.

### Leveransepakke

The maven-assembly-plugin is configured to create a Leveransepakke compatible artifact from the build (basically just
a .zip-file with all application jars). What is included in the file is configured in 
```src/main/assembly/leveransepakke_descriptor.xml```.

Note that the spring-boot-maven-plugin is configured to exclude the ```repackage``` goal (which is enabled by
default). The repackage goal will create an "uberjar" that includes the application and all its dependencies instead
of a jar file with only the application classes and resources. If this behaviour is not disabled all application
dependencies will be included in the Leveransepakke twice; once in the application jar from the repackage goal, and
once from the maven-assembly-plugin.

For more details, see
 * [Hvordan lage en leveransepakke som fungerer på OpenShift](https://aurora/wiki/pages/viewpage.action?pageId=112132497)

### Versioning

The pom.xml is configured with the aurora-cd-plugin for versioning. aurora-cd, in turn, uses the aurora-git-version
component. See [aurora-git-version](https://aurora/git/projects/AUF/repos/aurora-git-version/browse) for details.

Maven does not allow plugins to change the version of the current build, so the plugin has to be triggered once
before the actual "main" build is started. This is handled by Jenkins via the Jenkinsfile. See the ```Jenkinsfile``` in
the root folder and [aurora-pipeline-scripts](https://aurora/git/projects/AO/repos/aurora-pipeline-scripts/browse) for 
details.

### Code Analysis

Plugins for code analysis via Checkstyle, Sonar, Jacoco and PiTest are included in the pom. Checkstyle is configured 
with the default rule set for Skatteetaten. All code analysis is runn via the standard Jenkins pipeline scripts. See 
section on Jenkinsfile for more details.

### Jenkinsfile

For more details, see
 * [Hvordan ta i bruk Jenkins2 med jenkinsfile  ](https://aurora/wiki/display/OS/Hvordan+ta+i+bruk+Jenkins2+med+jenkinsfile)

### Nexus IQ

Every application that is deployed into production in the Skatteetaten networks are required to run a security
check via the Nexus IQ tool. A profile for performing this check is included in the pom, but you will need to acquire
your own staging profile id. See [reference missing]() for more details.

TODO: add reference to Nexus IQ docs.

### Build Metadata for Docker Images
Build data for docker images is read from the docker part of the ```src/main/assembly/metadata/openshift.json```-file. 
 
 * maintainer will be set as the MAINTAINER instruction in the generated docker image
 * all the labels in the labels object will be added as LABEL instructions in the generated docker image
  
 
## Development Tools

It is recommended to take a look at the productivity features of spring boot developer tools. See
[Developer Tools](http://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html).

Also, note that the developer-tools artifact will be removed by the Leveransepakkebygger when building Docker images
with semantic versions (for instance 1.3.3), and hence only be available for -SNAPSHOT builds.