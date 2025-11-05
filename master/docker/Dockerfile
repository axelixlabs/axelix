FROM eclipse-temurin:17

WORKDIR /app

# We expect a single executable JAR file to be downloaded in the GitHub Actions pipeline
COPY master/build/libs/master.jar /app/master.jar

EXPOSE 8080

# TODO: for now we do not validate certificates, we need to fix it later
CMD ["java", "-Dkubernetes.trust.certificates=true", "-jar", "/app/master.jar"]