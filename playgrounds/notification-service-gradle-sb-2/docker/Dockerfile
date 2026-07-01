FROM eclipse-temurin:21

WORKDIR /app

COPY build/libs/notification-service-0.0.2-SNAPSHOT.jar /app/notification-service.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/notification-service.jar"]