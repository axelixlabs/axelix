---
sidebar_position: 1
---

# What is Axelix Starter?

Axelix Starter (also known as ASBS - Axelix Spring Boot Starter) is a Spring Boot starter dependency that enables your
Spring Boot applications to integrate seamlessly with the Axelix platform. It's the bridge between your applications
and the Axelix Master management console.

## Overview

Axelix Starter transforms your standard Spring Boot application into a "managed service" that can be monitored, debugged, and controlled through the Axelix Master interface. It works by extending Spring Boot's native capabilities and providing additional endpoints and functionality.

## Key Capabilities

### **Automatic Service Discovery & Registration**
- **Automatic Discovery**: Axelix Master automatically register instances
- **Multi-Environment Support**: Works in Kubernetes, Docker Compose 
- [//]: # (TODO)
- **Self-Healing Registration**: Automatic re-registration if connections are interrupted

### **Enhanced Monitoring & Debugging**
- **Real-time Bean Analysis**: View all Spring beans.
- **Configuration Property Inspection**: See property values and their sources
- **Runtime Metrics**: Application performance and health metrics
- **State export**: Export state of application instance (including sanitize heap-dump, thread-dump, env, etc.)

## When to Use Axelix Starter

### **Development & Testing**
- **Debug configuration issues** in different environments (local, staging, production)
- **Understand bean wiring and dependencies** without deep code inspection
- **Test configuration changes** without redeployment or restarts
- **Validate property resolution** across multiple configuration sources
- **Toggle Scheduled Tasks**: Test different scheduling scenarios by enabling/disabling `@Scheduled` methods
- **Cache Management**: Verify cache behavior by clearing or disabling caches during testing

### **Production Operations**
- **Monitor application health** across multiple environments and instances
- **Perform emergency debugging** during production incidents
- **Manage configuration** across multiple application instances
- **Audit application state and configuration** for troubleshooting