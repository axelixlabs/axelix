FROM eclipse-temurin:17

WORKDIR /app

COPY target/spring-petclinic-2.7.18-SNAPSHOT.jar /app/petclinic.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/petclinic.jar"]
