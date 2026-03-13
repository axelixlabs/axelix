# Stage: extract Spring Boot application layers
FROM eclipse-temurin:25-jre-alpine AS layers

WORKDIR /application

# Copy distributions
COPY front-end/dist dist
COPY master/build/libs/master.jar master.jar

RUN java -Djarmode=layertools -jar master.jar extract

# Stage: AOT Cache training (JEP 483 — Ahead-of-Time Class Loading & Linking)
FROM eclipse-temurin:25-jre-alpine AS training
WORKDIR /application

COPY --from=layers /application/dependencies/ ./
COPY --from=layers /application/spring-boot-loader/ ./
COPY --from=layers /application/snapshot-dependencies/ ./
COPY --from=layers /application/application/ ./
COPY --from=layers /application/dist/ ./dist

# Step 1: Record class loading profile (training run — app may crash without infra, that's expected)
RUN java \
    -XX:AOTMode=record \
    -XX:AOTConfiguration=app.aotconf \
    -Dspring.main.web-application-type=none \
    -Daxelix.master.web.static-resources.location=file:/application/dist/ \
    -Dspring.context.exit=onRefresh \
    org.springframework.boot.loader.launch.JarLauncher

# Step 2: Create AOT cache from the recorded profile (does not execute application code)
RUN java \
    -XX:AOTMode=create \
    -XX:AOTConfiguration=app.aotconf \
    -XX:AOTCache=app.aot \
    org.springframework.boot.loader.launch.JarLauncher

# Stage: final runtime image
FROM eclipse-temurin:25-jre-alpine AS final

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

# Copy the front-end static files distribution (path must match static-locations below)
COPY --from=layers /application/dist/ ./dist

# Copy AOT cache from training stage
COPY --from=training /application/app.aot ./

# JVM options
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"
ENV JAVA_HEAP_DUMP_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp"
ENV JAVA_ON_OUT_OF_MEMORY_OPTS="-XX:+CrashOnOutOfMemoryError"
ENV JAVA_GC_LOG_OPTS="-Xlog:gc*,safepoint:/tmp/gc.log::filecount=10,filesize=100M"
# Custom Java Properties
ENV JAVA_OTHER_ARGS="-Dkubernetes.trust.certificates=true \
                     -Daxelix.master.web.static-resources.location=file:/application/dist/"

ENTRYPOINT exec java \
    -XX:AOTCache=app.aot \
    $JAVA_OTHER_ARGS \
    $JAVA_HEAP_DUMP_OPTS \
    $JAVA_ON_OUT_OF_MEMORY_OPTS \
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_GC_LOG_OPTS \
    org.springframework.boot.loader.launch.JarLauncher
