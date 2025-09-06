# ===== 1) Build stage =====
FROM --platform=$BUILDPLATFORM gradle:8.4-jdk17 AS build

# Gradle user home for caching
ENV GRADLE_USER_HOME=/home/gradle/.gradle
WORKDIR /home/gradle/app

# 1-1. Copy wrapper/metadata first to leverage cache
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle build.gradle settings.gradle ./
RUN chmod +x gradlew

# 1-2. Warm up dependencies cache (with a simple retry)
RUN ./gradlew --no-daemon -Dorg.gradle.jvmargs="-Xmx1024m" \
    -Dorg.gradle.internal.http.socketTimeout=60000 \
    -Dorg.gradle.internal.http.connectionTimeout=60000 \
    dependencies || ./gradlew --no-daemon dependencies

# 1-3. Copy source and build
COPY --chown=gradle:gradle src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ===== 2) Runtime stage =====
FROM --platform=$TARGETPLATFORM eclipse-temurin:17-jre

WORKDIR /app
# Copy the built jar (adjust the wildcard if your jar name changes)
COPY --from=build /home/gradle/app/build/libs/*-SNAPSHOT.jar /app/app.jar

# Render will inject $PORT; bind Spring to it
ENV JAVA_OPTS=""
ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default} -jar /app/app.jar"]