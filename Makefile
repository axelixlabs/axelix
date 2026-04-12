# MASTER/STARTER
build-all: build-master build-starter build-playgrounds

build-master:
	./gradlew :master:clean spotless :master:build

build-starter:
	./gradlew :starter:clean spotless :starter:build

run-master:
	./gradlew :master:clean :master:bootRun

clean-all: clean clean-playgrounds

clean:
	./gradlew clean

spotless:
	./gradlew spotlessApply

publish-local:
	./gradlew publishNexusPublicationMavenLocal


# PLAYGROUNDS

# Build
build-playgrounds: build-petclinic-maven build-petclinic-gradle build-notification-gradle build-feature-maven

build-petclinic-maven:
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean verify

build-petclinic-gradle:
	cd playgrounds/petclinic-gradle-sb-2 && ./gradlew clean build

build-notification-gradle:
	cd playgrounds/notification-gradle-sb-3 && ./gradlew clean build

build-feature-maven:
	cd playgrounds/feature-maven-sb-3 && ./mvnw clean spotless:apply verify

# Run
run-petclinic-maven:
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean spring-boot:run

run-petclinic-gradle:
	cd playgrounds/petclinic-gradle-sb-2 && ./gradlew clean bootRun

run-notification-gradle:
	cd playgrounds/notification-gradle-sb-3 && ./gradlew clean bootRun

run-feature-maven:
	cd playgrounds/feature-maven-sb-3 && ./mvnw clean spring-boot:run

# Clean
clean-playgrounds: clean-petclinic-maven clean-petclinic-gradle clean-notification-gradle clean-feature-maven

clean-petclinic-maven:
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw clean

clean-petclinic-gradle:
	cd playgrounds/petclinic-gradle-sb-2 && ./gradlew clean

clean-notification-gradle:
	cd playgrounds/notification-gradle-sb-3 && ./gradlew clean

clean-feature-maven:
	cd playgrounds/feature-maven-sb-3 && ./mvnw clean

# Reload/Sync
reload-petclinic-maven:
	cd playgrounds/petclinic-maven-sb-2 && ./mvnw dependency:resolve

sync-petclinic-gradle:
	cd playgrounds/petclinic-gradle-sb-2 && ./gradlew --refresh-dependencies dependencies

sync-notification-gradle:
	cd playgrounds/notification-gradle-sb-3 && ./gradlew --refresh-dependencies dependencies

reload-feature-maven:
	cd playgrounds/feature-maven-sb-3 && ./mvnw dependency:resolve

.PHONY: build-all build-master run-master clean clean-all spotless publish-local build-starter build-playgrounds \
	build-petclinic-maven build-petclinic-gradle build-notification-gradle build-feature-maven \
	run-petclinic-maven run-petclinic-gradle run-notification-gradle run-feature-maven \
	clean-playgrounds clean-petclinic-maven clean-petclinic-gradle clean-notification-gradle clean-feature-maven \
	reload-petclinic-maven sync-petclinic-gradle sync-notification-gradle reload-feature-maven
