.PHONY: clean clean-all clean-playgrounds spotless publish-local \
        build build-with-playgrounds build-playground \
        publish-starter-sb2 build-petclinic-maven-sb2 spotless-all

BUILD_ALL_PLAYGROUNDS ?= false
BUILD_SB2             ?= false

clean:
	./gradlew clean

clean-playgrounds:
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean

clean-all: clean clean-playgrounds

spotless:
	./gradlew spotlessApply

spotless-all:
	@echo "=== Formatting Backend Project ==="
	./gradlew spotlessApply
	@echo "=== Formatting Petclinic Maven SB-2 Project ==="
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw spring-javaformat:apply

publish-local:
	./gradlew publishToMavenLocal

build:
	@echo "=== Running Backend Build ==="
	./gradlew build

build-all: build
	$(MAKE) build-playground BUILD_ALL_PLAYGROUNDS="true"

# BUILD PLAYGROUND PROJECTS
build-playground:
	@echo "=== Starting Conditional Monorepo Build ==="

ifeq ($(BUILD_ALL_PLAYGROUNDS),true)
	@echo "=== Build all playgrounds ==="
	$(MAKE) publish-starter-sb2
	$(MAKE) build-petclinic-maven-sb2
else ifeq ($(BUILD_SB2),true)
	@echo "=== Build Petclinic Maven Spring boot 2 ==="
	$(MAKE) publish-starter-sb2
	$(MAKE) build-petclinic-maven-sb2
endif

publish-starter-sb2:
	@echo "=== Compiling and publishing Spring Boot 2 Axelix Starter ==="
	./gradlew :sbs:axelix-spring-boot-2-starter:publishToMavenLocal

build-petclinic-maven-sb2:
	@echo "=== Running Maven build for Petclinic SB 2 ==="
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean package -B