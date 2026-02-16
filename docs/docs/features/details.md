---
sidebar_position: 1
---

# Details{#main}

The Application Details page provides information about a managed Spring Boot application instance.
This page displays technical metadata, runtime information, and build details in an organized, easy-to-read format.

![details main page](../../static/img/feature/details/details-main-page.png)
***Details page as presented in Axile UI***

---

## Service Information Inner Header{#details}
- **Instance Name**: The name of the application instance. The postfix may vary depending on the runtime 
and discovery environment of axile-master.
- **Download state** Export application state as a ZIP archive containing selected diagnostic components.
The export includes various application state snapshots that can be used for debugging, analysis, or compliance purposes.

### Download state Components{#download}
The download dialog allows you to select which components to include in the export:

![details download state](../../static/img/feature/details/details-download-state.gif)

- **Heap Dump** - JVM Heap memory snapshot
    - *Sanitization Option*: Can be sanitized to remove sensitive data **(Available in Enterprise)**
- **Thread Dump** - Current thread states and stack traces
- **Beans** - Spring Beans information
- **Caches** - Cache contents
- **Conditions** - Spring condition evaluation results
- **Configuration Properties** - Spring Configuration Properties
- **Environment** - Application environment
- **Log File** - Current application log file
- **Scheduled Tasks** - Information about scheduled jobs

---

## Git Information{#git}
Displays source control metadata (when available):

| Field                | Description              | Example                |
|----------------------|--------------------------|------------------------|
| **Commit SHA**       | Short commit hash        | `a1b2c3d`              |
| **Branch**           | Git branch name          | `main`                 |
| **Author**           | Commit author name       | `John Doe`             |
| **Author Email**     | Commit author email      | `john@example.com`     |
| **Commit Timestamp** | When the commit was made | `2024-01-15T10:15:30Z` |

---

## Build Information{#build}
Shows details about the application build (when available):

| Field          | Description                    | Example                       |
|----------------|--------------------------------|-------------------------------|
| **Artifact**   | Maven/Gradle artifact ID       | `spring-petclinic`            |
| **Group**      | Project group ID               | `org.springframework.samples` |
| **Version**    | Application version            | `3.2.0`                       |
| **Build Time** | When the application was built | `2024-01-15T14:30:22Z`        |

---

## Runtime Environment Information{#runtime}
Detailed Java and runtime information:

| Field                 | Description                                        | Example                                    |
|-----------------------|----------------------------------------------------|--------------------------------------------|
| **Java Version**      | JVM version                                        | `17.0.17`                                  |
| **Kotlin Version**    | Kotlin version <br/> (if the instance uses Kotlin) | `1.9.22`                                   |
| **JDK Vendor**        | Java distribution vendor                           | `Liberica`, `OpenJDK`, `Amazon Corretto`   |
| **Garbage Collector** | Active GC algorithm                                | `G1`, `ZGC`, `GEN_SHENADOAH`, `EPSILONGC`  |

---

## Spring Framework Details{#spring}
Spring ecosystem versions:

| Field                        | Description                    | Example    |
|------------------------------|--------------------------------|------------|
| **Spring Boot Version**      | Spring Boot framework version  | `3.3.0`    |
| **Spring Framework Version** | Core Spring Framework version  | `6.2.0`    |
| **Spring Cloud Version**     | Spring Cloud version (if used) | `2023.0.0` |

---

## Operating System Information{#os}
System-level details:

| Field            | Description              | Example                   |
|------------------|--------------------------|---------------------------|
| **OS Family**    | Operating system name    | `Linux`, `Windows Server` |
| **OS Version**   | Operating system version | `5.15.0`, `2022`          |
| **Architecture** | CPU architecture         | `amd64`, `arm64`          |  