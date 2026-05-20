# feature-service
The feature-service microservice manages products, releases and features.

## TechStack
* Java, Spring Boot
* Flyway, Spring Data JPA
* Spring Security OAuth 2
* Maven, JUnit 5, Testcontainers
* Either PostgreSQL (defualt) or H2 in-memory database

### H2 Profile

In order to use an in-memory h2 database just launch the application with `h2` spring profile.

In case `h2` profile is enabled, running PostgreSQL database is not needed.

## Prerequisites
* JDK 21 or later
* Docker ([installation instructions](https://docs.docker.com/engine/install/))
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)
* Keycloak instance
* PostgrSQL instance (if applicable) 
 
Refer [docker-compose based infra setup](https://github.com/feature-tracker/docker-infra) for running dependent services.

## How to get started?

```shell
$ git clone https://github.com/feature-tracker/feature-service.git
$ cd feature-service

# Run tests
$ ./mvnw verify

# Format code
$ ./mvnw spotless:apply

# Run application
# Once the dependent services (PostgreSQL, Keycloak, etc) are started, 
# you can run/debug FeatureServiceApplication.java from your IDE.
```
