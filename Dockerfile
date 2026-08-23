# STAGE 1: BUILD
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew

# Tải dependencies trước - tận dụng Docker Layer Cache
RUN ./gradlew dependencies --no-daemon || return 0

COPY src src

RUN ./gradlew bootJar --no-daemon -x test


# STAGE 2: RUNTIME
FROM eclipse-temurin:21-jre-jammy AS runtime

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080


HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -q -O /dev/null -S "http://localhost:8080/" 2>&1 | grep -q "HTTP/" || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]