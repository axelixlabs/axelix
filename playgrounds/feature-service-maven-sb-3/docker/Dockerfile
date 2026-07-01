FROM eclipse-temurin:24

WORKDIR /app

COPY target/feature-service-0.0.2-SNAPSHOT.jar /app/feature-service.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/feature-service.jar"]