# Stage: extract Spring Boot application layers
FROM eclipse-temurin:25-jre-alpine AS layers
WORKDIR /application
COPY master/build/libs/master.jar master.jar
RUN java -Djarmode=layertools -jar master.jar extract

# Stage: final runtime image
FROM eclipse-temurin:25-jre-alpine

VOLUME /tmp

# Configure non-root user
RUN adduser -S axelix
USER axelix

WORKDIR /application

# Copy Spring Boot application layers
COPY --from=layers /application/dependencies/ ./
COPY --from=layers /application/spring-boot-loader/ ./
COPY --from=layers /application/snapshot-dependencies/ ./
COPY --from=layers /application/application/ ./

# JVM options
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"
ENV JAVA_HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV JAVA_ON_OUT_OF_MEMORY_OPTS="-XX:+CrashOnOutOfMemoryError"
ENV JAVA_GC_LOG_OPTS="-Xlog:gc*,safepoint:/tmp/gc.log::filecount=10,filesize=100M"

# TODO: Consider adding AOT Cache
# Java Properties
ENV JAVA_OTHER_ARGS="-Dkubernetes.trust.certificates=true"

ENTRYPOINT exec java \
    $JAVA_OTHER_ARGS \
    $JAVA_HEAP_DUMP_OPTS \
    $JAVA_ON_OUT_OF_MEMORY_OPTS \
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_GC_LOG_OPTS \
    org.springframework.boot.loader.JarLauncher
