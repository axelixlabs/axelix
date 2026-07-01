.PHONY: clean clean-all clean-playgrounds spotless publish-local build build-with-playgrounds \
        build-playground publish-starter-sb2 build-petclinic-maven-sb2 spotless-all publish-starter-sb3 \
        build-feature-service-maven-sb3

BUILD_ALL_PLAYGROUNDS ?= false
BUILD_SB2             ?= false
BUILD_SB3             ?= false

clean:
	./gradlew clean

clean-playgrounds:
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean
	cd playgrounds/feature-service-maven-sb-3 && ./mvnw clean

clean-all: clean clean-playgrounds

spotless:
	./gradlew spotlessApply

spotless-all:
	@echo "=== Formatting Backend Project ==="
	./gradlew spotlessApply
	@echo "=== Formatting Petclinic Maven SB-2 Project ==="
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw spring-javaformat:apply
	@echo "=== Formatting Feature Service Maven SB-3 Project ==="
	cd playgrounds/feature-service-maven-sb-3 && ./mvnw spotless:apply

publish-local:
	./gradlew publishToMavenLocal

build:
	@echo "=== Running Backend Build ==="
	./gradlew build

build-all: build
	$(MAKE) build-playground BUILD_ALL_PLAYGROUNDS="true"

# BUILD PLAYGROUND PROJECTS
build-playground:

ifeq ($(BUILD_ALL_PLAYGROUNDS),true)
	@echo "=== Build all Playgrounds ==="
	$(MAKE) publish-starter-sb2
	$(MAKE) build-petclinic-maven-sb2
	$(MAKE) publish-starter-sb3
	$(MAKE) build-feature-service-maven-sb3
else ifeq ($(BUILD_SB2),true)
	@echo "=== Publish Axelix Spring boot 2 Starter ==="
	$(MAKE) publish-starter-sb2
	@echo "=== Build Petclinic Maven Spring boot 2 ==="
	$(MAKE) build-petclinic-maven-sb2
else ifeq ($(BUILD_SB3),true)
	@echo "=== Publish Axelix Spring boot 3 Starter ==="
	$(MAKE) publish-starter-sb3
	@echo "=== Build Feature Service Maven Spring boot 3 ==="
	$(MAKE) build-feature-service-maven-sb3
endif

publish-starter-sb2:
	@echo "=== Compiling and publishing Spring Boot 2 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-2-starter:publishToMavenLocal

publish-starter-sb3:
	@echo "=== Compiling and publishing Spring Boot 3 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-3-starter:publishToMavenLocal

build-petclinic-maven-sb2:
	@echo "=== Running Maven build for Petclinic Spring boot 2 ==="
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean package -B

build-feature-service-maven-sb3:
	@echo "=== Running Maven build for Feature Service Spring boot 3 ==="
	cd playgrounds/feature-service-maven-sb-3 && ./mvnw clean package -B