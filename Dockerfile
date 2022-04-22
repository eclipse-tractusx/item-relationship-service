# Dependencies
FROM maven:3-openjdk-17 AS maven
ARG BUILD_TARGET=irs-api

WORKDIR /build

COPY ci ci
COPY api api
COPY .mvn .mvn
COPY settings.xml .
COPY pom.xml .

COPY connector/pom.xml connector/pom.xml
COPY connector/edc-recursive-job connector/edc-recursive-job
COPY connector/irs-connector-parent connector/irs-connector-parent
COPY integration-tests integration-tests
COPY irs-api irs-api
COPY irs-common irs-common
COPY irs-models irs-models
COPY irs-parent irs-parent
COPY irs-parent-spring-boot irs-parent-spring-boot
COPY irs-testing irs-testing

# the --mount option requires BuildKit.
RUN --mount=type=cache,target=/root/.m2 mvn -B -s settings.xml clean package -pl :$BUILD_TARGET -am -DskipTests


# Copy the jar and build image
FROM eclipse-temurin:17-jre AS irs-api

WORKDIR /app

COPY --from=maven /build/irs-api/target/irs-api-*-exec.jar app.jar

ENTRYPOINT ["java", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]