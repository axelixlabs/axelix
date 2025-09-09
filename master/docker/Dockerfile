FROM openjdk:17

WORKDIR /app

# We expect a single executable JAR file in the gradle build output directory
COPY master.jar /app/master.jar

CMD ["java", "-jar", "/app/master.jar"]