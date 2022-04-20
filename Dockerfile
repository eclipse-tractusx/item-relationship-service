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

# Download JMX prometheus agent
FROM curlimages/curl:7.82.0 AS curl

# jmx exporter version: https://github.com/prometheus/jmx_exporter/releases
ARG jmxPrometheusAgentVersion="0.16.1"
ARG jmxPrometheusAgentURL="https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${jmxPrometheusAgentVersion}/jmx_prometheus_javaagent-${jmxPrometheusAgentVersion}.jar"

# Download jmx exporter agent (for non-spring boot applications)
RUN curl --silent -o jmx-prometheus-agent.jar $jmxPrometheusAgentURL

# Copy the jar and build image
FROM eclipse-temurin:17-jre AS irs-api

WORKDIR /app

COPY --from=maven /build/irs-api/target/irs-api-*-exec.jar app.jar

COPY --from=curl jmx-prometheus-agent.jar .
COPY cd/jmx_prometheus_config.yml .

ENTRYPOINT ["java", "-javaagent:./jmx-prometheus-agent.jar=4006:./jmx_prometheus_config.yml", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]