FROM openjdk:17

ARG NAME="master"
ARG VERSION

WORKDIR /app

# We expect a single executable JAR file in the gradle build output directory
COPY build/libs/master.jar /app/master.jar

CMD ["java", "-jar", "/app/master.jar"]