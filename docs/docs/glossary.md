---
sidebar_position: 7
---

# Glossary

This document defines key terms and abbreviations used throughout the Axile documentation and codebase.

## Terms

### Axile
The name of the project. Also used as an umbrella term referring to the entire system as a whole.

### Axile Master
An independent application deployed separately that:
- Provides the web UI for Axile
- Interacts with managed services
- Serves as the central management point
- Handles service discovery and registration
- 
### Axile Spring Boot Starter (ASBS)
A Spring Boot starter that is included in Spring Boot applications to enable Axile integration.
It provides the necessary capabilities that are called by the Axile Master.

**Aliases:** ASBS, Axile SBS

### Managed Service
A Spring Boot application that:
- Has the Axile Spring Boot Starter integrated
- Is registered with and known to Axile Master
- Can be monitored and managed through Axile