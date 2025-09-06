# syntax=docker/dockerfile:1

# ===== 1) Build stage =====
FROM gradle:8.8-jdk17 AS build
WORKDIR /workspace

# gradle wrapper 먼저 복사(캐시 극대화)
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

# 나머지 소스 복사 후 빌드
COPY . .
RUN ./gradlew --no-daemon clean bootJar -x test

# ===== 2) Runtime stage =====
FROM eclipse-temurin:17-jre-jammy
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Xms256m -Xmx512m"
WORKDIR /app

# 빌드 산출물 복사 (프로젝트별 JAR명이 다를 수 있어 와일드카드 사용)
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar /app/app.jar

# Render가 PORT 환경변수를 넘겨줌 → 그 포트로 바인딩
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /app/app.jar"]