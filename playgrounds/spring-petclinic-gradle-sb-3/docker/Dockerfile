FROM eclipse-temurin:17

WORKDIR /app

COPY build/libs/spring-petclinic-3.5.0-SNAPSHOT.jar /app/petclinic.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/petclinic.jar"]
