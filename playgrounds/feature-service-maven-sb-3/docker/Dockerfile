# Stage: Spring Boot layer extraction for AOT-cache-friendly runtime layout
FROM eclipse-temurin:25 AS training

WORKDIR /application

COPY target/feature-service-0.0.2-SNAPSHOT.jar /application/application.jar

# Extract Spring Boot layers so training and runtime use the same layout
RUN java \
    -Djarmode=tools \
    -jar application.jar extract \
    --layers \
    --destination extracted

# Stage: final runtime image
FROM eclipse-temurin:25

WORKDIR /application

COPY --from=training /application/extracted/dependencies/ ./
COPY --from=training /application/extracted/spring-boot-loader/ ./
COPY --from=training /application/extracted/snapshot-dependencies/ ./
COPY --from=training /application/extracted/application/ ./

# Step 1: Record class loading profile
RUN java \
    -XX:+UseCompactObjectHeaders \
    -XX:AOTMode=record \
    -XX:AOTConfiguration=application.aotconf \
    -Daxelix.sbs.auth.jwt.algorithm=HMAC256 \
    -Daxelix.sbs.auth.jwt.signing-key=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn \
    -Dspring.context.exit=onRefresh \
    -jar /application/application.jar

# Step 2: Create AOT cache from the recorded profile
RUN java \
    -XX:+UseCompactObjectHeaders \
    -XX:AOTMode=create \
    -XX:AOTConfiguration=application.aotconf \
    -XX:AOTCache=application.aot \
    -jar /application/application.jar

EXPOSE 8080

# JVM options
ENV JAVA_GC_LOG_OPTS="-Xlog:gc=info,safepoint:gc.log::filecount=1,filesize=20M"
ENV JAVA_AOT_CACHE_OPTS="-XX:+UseCompactObjectHeaders -XX:AOTCache=/application/application.aot"

ENTRYPOINT exec java \
    $JAVA_AOT_CACHE_OPTS \
    $JAVA_GC_LOG_OPTS \
    -jar /application/application.jar
