# notification-service
The notification-service microservice listens to various events and notifies the interested parties via email.

## TechStack
* Kotlin, Spring Boot
* MariaDB, Flyway, Spring Data JDBC
* Gradle Kts, JUnit 5, Testcontainers

## Prerequisites
* JDK 21 or later
* Docker ([installation instructions](https://docs.docker.com/engine/install/))
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)
* MariaDB and Kafka

Refer [docker-compose based infra setup](https://github.com/feature-tracker/docker-infra) for running dependent services.

## How to get started?

```shell
$ git clone https://github.com/feature-tracker/notification-service.git
$ cd notification-service

# Run tests
$ ./gradlew test

# Format code
$ ./gradlew spotlessApply

# Run application
# Once the dependent services (MariaDB, Kafka, etc) are started, 
# you can run/debug NotificationServiceApplication.java from your IDE.
```
