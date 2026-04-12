FROM eclipse-temurin:24

WORKDIR /app

COPY target/feature-maven-0.0.2-SNAPSHOT.jar /app/feature-maven.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/feature-maven.jar"]