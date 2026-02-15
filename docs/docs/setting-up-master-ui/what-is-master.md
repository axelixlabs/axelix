---
sidebar_position: 1
---

# What is Master?

Axelix Master (or simply "Master") is the central hub of the Axelix ecosystem, application with enhanced capabilities for managing and monitoring Spring Boot applications across various environments.

## Core Purpose

Axelix Master serves as a **centralized management console** that automatically discovers, registers, and monitors Spring Boot applications, providing real-time insights and control.

## Key Capabilities

### 🔍 **Automatic Service Discovery**
Master automatically finds and registers Spring Boot applications in your environment:

**Supported Environments:**
- ✅ **Kubernetes**
- ✅ **Docker Compose**
- 🔄 **Additional environments coming soon**

**How Discovery Works:**
1. **Environment Detection**: Master identifies the runtime environment
2. **Service Scanning**: Probes for services with Axelix SBS
3. **Auto-Registration**: Registers qualifying services automatically
4. **Health Monitoring**: Periodically polls registered services for status

### 🖥️ **Unified Web Interface**
A single pane of glass for all your Spring Boot applications:

**Features include:**
- Dashboard with all registered applications
- Real-time health status and metrics
- Configuration property and Environment inspection
- Detailed analysis Beans
- and so on, see the Features section

### 🔄 **Service Proxy & Orchestration**
Master acts as a smart proxy between users and managed services: