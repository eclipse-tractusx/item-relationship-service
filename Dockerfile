# Dependencies
FROM maven:3-openjdk-17-slim AS maven
ARG BUILD_TARGET=irs-api

WORKDIR /build

COPY ci ci
COPY api api
COPY .mvn .mvn
COPY pom.xml .

COPY integration-tests integration-tests
COPY irs-api irs-api
COPY irs-models irs-models
COPY irs-parent-spring-boot irs-parent-spring-boot
COPY irs-testing irs-testing
COPY irs-report-aggregate irs-report-aggregate
COPY cucumber-tests cucumber-tests
COPY docs docs

# the --mount option requires BuildKit.
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -pl :$BUILD_TARGET -am -DskipTests


# Copy the jar and build image
FROM eclipse-temurin:17-jre-alpine AS irs-api

ARG UID=10000
ARG GID=1000

WORKDIR /app

COPY --chmod=755 --from=maven /build/irs-api/target/irs-api-*-exec.jar app.jar

USER ${UID}:${GID}

ENTRYPOINT ["java", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]

HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:4004/actuator/health || exit 1