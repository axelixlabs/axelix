# Stage: Spring Boot layer extraction for CDS-friendly runtime layout
FROM eclipse-temurin:17 AS training

WORKDIR /application

COPY build/libs/api-gateway-gradle-sb-4-1.2.0-SNAPSHOT.jar /application/application.jar

# Extract Spring Boot layers so training and runtime use the same layout
RUN java \
    -Djarmode=tools \
    -jar application.jar extract \
    --layers \
    --destination extracted

# Stage: final runtime image
FROM eclipse-temurin:17

WORKDIR /application

COPY --from=training /application/extracted/dependencies/ ./
COPY --from=training /application/extracted/spring-boot-loader/ ./
COPY --from=training /application/extracted/snapshot-dependencies/ ./
COPY --from=training /application/extracted/application/ ./

# Execute the CDS training run. The JWT key comes from the bundled application.properties, so the
# context refreshes without any extra -D flags.
RUN java \
    -XX:ArchiveClassesAtExit=/application/application.jsa \
    -Dspring.context.exit=onRefresh \
    -jar /application/application.jar

EXPOSE 8096

# JVM options
ENV JAVA_GC_LOG_OPTS="-Xlog:gc=info,safepoint:gc.log::filecount=1,filesize=20M"
ENV JAVA_APP_CDS_OPTS="-XX:SharedArchiveFile=/application/application.jsa"

ENTRYPOINT exec java \
    $JAVA_APP_CDS_OPTS \
    $JAVA_GC_LOG_OPTS \
    -jar /application/application.jar
