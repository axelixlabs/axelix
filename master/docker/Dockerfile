FROM openjdk:17

WORKDIR /app

# We expect a single executable JAR file in the gradle build output directory
COPY master/build/libs/master.jar /app/master.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/master.jar"]