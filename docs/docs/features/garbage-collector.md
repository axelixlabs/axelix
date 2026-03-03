---
sidebar_position: 12
---

# Garbage Collector

We provide convenient access to `Garbage Collector` logs in the Spring Boot application.

![garbage collector main page](../../static/img/feature/garbage-collector/gc-main-page.png)
***Garbage Collector as presented in Axelix UI***

In most cases, Garbage Collector logging is disabled by default. To view the logs, it must be enabled. 
The “Enable GC Logging” option allows you to do this.

<img src="/img/feature/garbage-collector/enable-gc-logging.png" alt="enable-gc-logging" width="490" height="280"/>

---


## Garbage Collector Details{#details}
After enabling Garbage Collector logging, you can select the logging level:
- INFO
- WARNING
- TRACE
- DEBUG
- ERROR

After selecting the logging level, you can:
1. Trigger <img src="/img/feature/icons/trigger-icon.png" width="30" height="19"/> 
   the Garbage Collector to generate logs, or wait for logs to be produced during normal execution.
2. Download <img src="/img/feature/icons/download-icon.png" width="30" height="19"/> 
   the generated logs as a `.txt` file.
3. Disable <img src="/img/feature/icons/disable-icon.png" width="30" height="19"/> GC logging.

```
[2026-03-02T17:38:55.009+0000][info][gc] GC(428) Pause Full (System.gc()) 29M->28M(97M) 82.955ms
              │                  │    │    │               │               │    │   │       │
              │                  │    │    │               │               │    │   │       └─ GC pause duration
              │                  │    │    │               │               │    │   └─ Total heap
              │                  │    │    │               │               │    └─ After heap
              │                  │    │    │               │               └─ Before heap
              │                  │    │    │               └─ GC event description
              │                  │    │    └─ GC event ID
              │                  │    └─ Log tag (subsystem)
              │                  └─ Log level
              └─ Timestamp (ISO-8601 with timezone)
```