# Dockerfile for the OSS (community) distribution of Axelix Master.
FROM eclipse-temurin:25-jre-alpine

VOLUME /tmp

RUN apk add --no-cache --upgrade \
    "openssl>=3.5.7-r0" \
    "libssl3>=3.5.7-r0" \
    "libcrypto3>=3.5.7-r0" \
    "libexpat>=2.8.4-r0"

# Configure non-root user
RUN adduser -S axelix
USER axelix

WORKDIR /application

COPY build/libs/master.jar master.jar

# JVM options
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"
ENV JAVA_HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV JAVA_ON_OUT_OF_MEMORY_OPTS="-XX:+CrashOnOutOfMemoryError"
ENV JAVA_GC_LOG_OPTS="-Xlog:gc*,safepoint:/tmp/gc.log::filecount=10,filesize=100M"

# TODO: The AOT cache was removed because the JVM crashed on startup with SIGILL on some CPUs.
#  Investigate and bring it back.
ENTRYPOINT exec java \
    $JAVA_HEAP_DUMP_OPTS \
    $JAVA_ON_OUT_OF_MEMORY_OPTS \
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_GC_LOG_OPTS \
    -cp master.jar \
    org.springframework.boot.loader.launch.JarLauncher
