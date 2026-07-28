.PHONY: clean clean-playgrounds clean-all build build-playground build-all spotless spotless-all \
        publish-local build-plugins publish-starter-sb-2 publish-starter-sb-3 \
        build-spring-petclinic-maven-sb-2 build-notification-service-gradle-sb-2 \
        build-feature-service-maven-sb-3 build-spring-petclinic-gradle-sb-3 publish-plugins \
        publish-gradle-plugin publish-maven-plugin

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

re-build:
	@echo "=== Running Backend Build ==="
	./gradlew build --no-build-cache --no-configuration-cache --rerun-tasks

build-plugins:
	./gradlew :plugins:axelix-gradle-plugin:build :plugins:axelix-maven-plugin:build

build-all: build build-plugins
	$(MAKE) build-playground BUILD_SB2="true" BUILD_SB3="true"

# BUILD PLAYGROUND PROJECTS
# Listed as plain prerequisites (not recursive $(MAKE) calls). Starter publication is modeled
# as each playground build's own prerequisite (see below), not listed here as a sibling - sibling
# prerequisites are not ordered under make -j, so that would let a build start before its starter
# is published.
PLAYGROUND_TARGETS :=
ifeq ($(BUILD_SB2),true)
PLAYGROUND_TARGETS += build-spring-petclinic-maven-sb-2 build-notification-service-gradle-sb-2
endif
ifeq ($(BUILD_SB3),true)
PLAYGROUND_TARGETS += build-spring-petclinic-gradle-sb-3 build-feature-service-maven-sb-3
endif

build-playground: publish-plugins $(PLAYGROUND_TARGETS)

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

LOCAL_JAVA_25 := $(firstword $(wildcard \
    $(JAVA_25_HOME) \
    $(HOME)/.jdks/liberica-25* \
    $(HOME)/.jdks/temurin-25* \
    $(HOME)/.sdkman/candidates/java/25*))

# PUBLISH STARTERS
publish-starter-sb-2:
	@echo "=== Publishing Spring Boot 2 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-2-starter:publishToMavenLocal

publish-starter-sb-3:
	@echo "=== Publishing Spring Boot 3 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-3-starter:publishToMavenLocal

# PUBLISH PLUGINS
publish-plugins: publish-gradle-plugin publish-maven-plugin

publish-gradle-plugin:
	@echo "=== Publishing Axelix Gradle Plugin ==="
	./gradlew :plugins:axelix-gradle-plugin:publishToMavenLocal

publish-maven-plugin:
	@echo "=== Publishing Axelix Maven Plugin ==="
	./gradlew :plugins:axelix-maven-plugin:publishToMavenLocal

# BUILD SPECIFIC PLAYGROUNDS
build-spring-petclinic-maven-sb-2: publish-maven-plugin publish-starter-sb-2
	@echo "=== Running Maven build for Petclinic Spring Boot 2 ==="
	cd playgrounds/spring-petclinic-maven-sb-2 && JAVA_HOME=$(LOCAL_JAVA_17) ./mvnw package -B

build-notification-service-gradle-sb-2: publish-gradle-plugin publish-starter-sb-2
	@echo "=== Running Gradle build for Notification Service Spring Boot 2 ==="
	cd playgrounds/notification-service-gradle-sb-2 && JAVA_HOME=$(LOCAL_JAVA_21) ./gradlew build

build-feature-service-maven-sb-3: publish-maven-plugin publish-starter-sb-3
	@echo "=== Running Maven build for Feature Service Spring Boot 3 ==="
	cd playgrounds/feature-service-maven-sb-3 && JAVA_HOME=$(LOCAL_JAVA_25) ./mvnw package -B

build-spring-petclinic-gradle-sb-3: publish-gradle-plugin publish-starter-sb-3
	@echo "=== Running Gradle build for Petclinic Spring Boot 3 ==="
	cd playgrounds/spring-petclinic-gradle-sb-3 && JAVA_HOME=$(LOCAL_JAVA_17) ./gradlew build