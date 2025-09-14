FROM openjdk:17

WORKDIR /app

# We expect a single executable JAR file to be downloaded in the GitHub Actions pipeline
COPY master.jar /app/master.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/master.jar"]