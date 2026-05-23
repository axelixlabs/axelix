FROM eclipse-temurin:21

WORKDIR /app

COPY build/libs/notification-gradle-sb-3-0.0.2-SNAPSHOT.jar /app/notification-gradle-sb-3.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/notification-gradle-sb-3.jar"]