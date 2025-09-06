# ===== 1) Build stage: Gradle이 이미 들어있는 이미지 사용 =====
FROM gradle:8.4-jdk17-alpine AS build
# ↑ 프로젝트의 gradle-wrapper.properties와 호환되는 버전으로 맞추세요.
#   (wrapper가 8.14.3라면 8.14.0+ 라인으로 올려도 됩니다: gradle:8.14.0-jdk17-alpine)

WORKDIR /workspace
# 권한 이슈 방지 (선택)
USER gradle
COPY --chown=gradle:gradle . .

# 캐시 활용해서 의존성 빠르게 받도록 (alpine에서도 잘 동작)
RUN gradle --no-daemon clean bootJar -x test

# ===== 2) Runtime stage =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# build 결과 JAR 복사 (프로젝트명/버전에 맞춰 경로 확인)
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# Render는 PORT 환경변수를 사용하지만, Spring이 8080에서 뜨면 안 됨.
# 이미 application.yml에 server.port: 8080이 있으면 Render가 포트 바인딩을
# 자동으로 못 찾을 수 있어요. 아래처럼 환경변수로 덮어쓰는 방식 권장.
ENV SERVER_PORT=10000
CMD ["sh", "-c", "java -jar -Dserver.port=${PORT:-$SERVER_PORT} /app/app.jar"]