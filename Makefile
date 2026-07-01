.PHONY: clean clean-playgrounds clean-all build build-playground build-all spotless spotless-all \
        publish-local publish-starter-sb-2 publish-starter-sb-3 \
        build-spring-petclinic-maven-sb-2 build-notification-service-gradle-sb-2 \
        build-feature-service-maven-sb-3 build-spring-petclinic-gradle-sb-3

BUILD_SB2             ?= true
BUILD_SB3             ?= true

clean:
	./gradlew clean

clean-playgrounds:
	cd playgrounds/spring-petclinic-maven-sb-2 && ./mvnw clean
	cd playgrounds/notification-service-gradle-sb-2 && ./gradlew clean
	cd playgrounds/feature-service-maven-sb-3 && ./mvnw clean
	cd playgrounds/spring-petclinic-gradle-sb-3 && ./gradlew clean

clean-all: clean clean-playgrounds

spotless:
	./gradlew spotlessApply

spotless-all:
	./gradlew spotlessApply
	cd playgrounds/spring-petclinic-maven-sb-2 && ./mvnw spring-javaformat:apply
	cd playgrounds/notification-service-gradle-sb-2 && ./gradlew spotlessApply
	cd playgrounds/feature-service-maven-sb-3 && ./mvnw spotless:apply
	cd playgrounds/spring-petclinic-gradle-sb-3 && ./gradlew spotlessApply

publish-local:
	./gradlew publishToMavenLocal

build:
	@echo "=== Running Backend Build ==="
	./gradlew build

build-all: build
	$(MAKE) build-playground BUILD_SB2="true" BUILD_SB3="true"

# BUILD PLAYGROUND PROJECTS
build-playground:

ifeq ($(BUILD_SB2),true)
	@echo "=== Publish Axelix Spring Boot 2 Starter ==="
	$(MAKE) publish-starter-sb-2
	@echo "=== Build Spring Petclinic Maven Spring Boot 2 ==="
	$(MAKE) build-spring-petclinic-maven-sb-2
	@echo "=== Build Notification Service Gradle Spring Boot 2 ==="
	$(MAKE) build-notification-service-gradle-sb-2
endif
ifeq ($(BUILD_SB3),true)
	@echo "=== Publish Axelix Spring Boot 3 Starter ==="
	$(MAKE) publish-starter-sb-3
	@echo "=== Build Spring Petclinic Gradle Spring Boot 3 ==="
	$(MAKE) build-spring-petclinic-gradle-sb-3
	@echo "=== Build Feature Service Maven Spring Boot 3 ==="
	$(MAKE) build-feature-service-maven-sb-3
endif

LOCAL_JAVA_17 := $(firstword $(wildcard \
    $(JAVA_17_HOME) \
    $(HOME)/.jdks/liberica-17* \
    $(HOME)/.jdks/temurin-17* \
    $(HOME)/.sdkman/candidates/java/17*))

LOCAL_JAVA_21 := $(firstword $(wildcard \
    $(JAVA_21_HOME) \
    $(HOME)/.jdks/liberica-21* \
    $(HOME)/.jdks/temurin-21* \
    $(HOME)/.sdkman/candidates/java/21*))

LOCAL_JAVA_24 := $(firstword $(wildcard \
    $(JAVA_24_HOME) \
    $(HOME)/.jdks/liberica-24* \
    $(HOME)/.jdks/temurin-24* \
    $(HOME)/.sdkman/candidates/java/24*))

# PUBLISH STARTERS
publish-starter-sb-2:
	@echo "=== Publishing Spring Boot 2 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-2-starter:publishToMavenLocal

publish-starter-sb-3:
	@echo "=== Publishing Spring Boot 3 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-3-starter:publishToMavenLocal

# BUILD SPECIFIC PLAYGROUNDS
build-spring-petclinic-maven-sb-2:
	@echo "=== Running Maven build for Petclinic Spring Boot 2 ==="
	cd playgrounds/spring-petclinic-maven-sb-2 && JAVA_HOME=$(LOCAL_JAVA_17) ./mvnw package -B

build-notification-service-gradle-sb-2:
	@echo "=== Running Gradle build for Notification Service Spring Boot 2 ==="
	cd playgrounds/notification-service-gradle-sb-2 && JAVA_HOME=$(LOCAL_JAVA_21) ./gradlew build

build-feature-service-maven-sb-3:
	@echo "=== Running Maven build for Feature Service Spring Boot 3 ==="
	cd playgrounds/feature-service-maven-sb-3 && JAVA_HOME=$(LOCAL_JAVA_24) ./mvnw package -B

build-spring-petclinic-gradle-sb-3:
	@echo "=== Running Gradle build for Petclinic Spring Boot 3 ==="
	cd playgrounds/spring-petclinic-gradle-sb-3 && JAVA_HOME=$(LOCAL_JAVA_17) ./gradlew build