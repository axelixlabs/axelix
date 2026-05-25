# Stage: AOT Cache training (JEP 483 — Ahead-of-Time Class Loading & Linking)
FROM eclipse-temurin:25-jre-alpine AS training

WORKDIR /application

COPY master/build/libs/master.jar master.jar

# Step 1: Record class loading profile (training run — app may crash without infra, that's expected)
RUN java \
    -XX:AOTMode=record \
    -XX:AOTConfiguration=app.aotconf \
    -Dspring.main.web-application-type=none \
    -Daxelix.master.auth.jwt.algorithm=HMAC256 \
    -Daxelix.master.auth.jwt.signing-key=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn \
    -Daxelix.master.discovery.auto.enabled=false \
    -Dspring.context.exit=onRefresh \
    -jar /application/master.jar

# Step 2: Create AOT cache from the recorded profile (does not execute application code)
RUN java \
    -XX:AOTMode=create \
    -XX:AOTConfiguration=app.aotconf \
    -XX:AOTCache=app.aot \
    -jar /application/master.jar

# Stage: final runtime image
FROM eclipse-temurin:25-jre-alpine AS final

VOLUME /tmp

# Configure non-root user
RUN adduser -S axelix
USER axelix

WORKDIR /application

COPY --from=training /application/master.jar ./
COPY --from=training /application/app.aot ./

# JVM options
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"
ENV JAVA_HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV JAVA_ON_OUT_OF_MEMORY_OPTS="-XX:+CrashOnOutOfMemoryError"
ENV JAVA_GC_LOG_OPTS="-Xlog:gc*,safepoint:/tmp/gc.log::filecount=10,filesize=100M"

ENTRYPOINT exec java \
    -XX:AOTCache=app.aot \
    $JAVA_HEAP_DUMP_OPTS \
    $JAVA_ON_OUT_OF_MEMORY_OPTS \
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_GC_LOG_OPTS \
    org.springframework.boot.loader.launch.JarLauncher
