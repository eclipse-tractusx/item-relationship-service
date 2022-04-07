# Dependencies
FROM maven:3-jdk-11 AS maven
ARG BUILD_TARGET=irs-api
ARG IRS_EDC_PKG_USERNAME
ARG IRS_EDC_PKG_PASSWORD

WORKDIR /build

COPY ci ci
COPY api api
COPY .mvn .mvn
COPY settings.xml .
COPY pom.xml .

COPY connector/pom.xml connector/pom.xml
COPY connector/edc-patched-core connector/edc-patched-core
COPY connector/edc-recursive-job connector/edc-recursive-job
COPY connector/edc-transfer-process-watchdog connector/edc-transfer-process-watchdog
COPY connector/irs-connector-commons connector/irs-connector-commons
COPY connector/irs-connector-parent connector/irs-connector-parent
COPY connector/irs-connector-testing connector/irs-connector-testing
COPY integration-tests integration-tests
COPY irs-api irs-api
COPY irs-client irs-client
COPY irs-common irs-common
COPY irs-models irs-models
COPY irs-parent irs-parent
COPY irs-parent-spring-boot irs-parent-spring-boot
COPY irs-testing irs-testing

# the --mount option requires BuildKit.
RUN --mount=type=cache,target=/root/.m2 mvn -B -s settings.xml clean package -pl :$BUILD_TARGET -am -DskipTests

# Download Application Insights agent
FROM maven:3-jdk-11 AS wget

# Java app insight agent version: https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-in-process-agent
# See https://confluence.catena-x.net/display/CXM/PRS+Observability
ARG appInsightsAgentVersion="3.2.3"
ARG appInsightsAgentURL="https://github.com/microsoft/ApplicationInsights-Java/releases/download/${appInsightsAgentVersion}/applicationinsights-agent-${appInsightsAgentVersion}.jar"

# jmx exporter version: https://github.com/prometheus/jmx_exporter/releases
ARG jmxPrometheusAgentVersion="0.16.1"
ARG jmxPrometheusAgentURL="https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${jmxPrometheusAgentVersion}/jmx_prometheus_javaagent-${jmxPrometheusAgentVersion}.jar"

# Download app insights agent
RUN wget -q -O applicationinsights-agent.jar $appInsightsAgentURL
# Download jmx exporter agent (for non-spring boot applications)
RUN wget -q -O jmx-prometheus-agent.jar $jmxPrometheusAgentURL

# Copy the jar and build image
FROM adoptopenjdk:11-jre-hotspot AS runtime

WORKDIR /app

COPY --from=wget applicationinsights-agent.jar .
COPY cd/applicationinsights.json .

ENTRYPOINT ["java", "-javaagent:./applicationinsights-agent.jar", "-jar", "app.jar"]

FROM runtime AS irs-api
COPY --from=maven /build/irs-api/target/irs-api-*-exec.jar app.jar

COPY --from=wget jmx-prometheus-agent.jar .
COPY cd/jmx_prometheus_config.yml .

# Settings for EDC FsVault (filesystem-based key vault)
# Creates an empty property file.
RUN cp /dev/null dataspaceconnector-vault.properties
# Create an empty Java Key Store: this requires creating a dummy entry, then deleting it
RUN keytool -genkey -noprompt -alias alias1  -dname "CN=dummy" -keystore dataspaceconnector-keystore.jks  -storepass test123 -keypass test123 \
   && keytool -delete -alias alias1 -storepass test123 -keystore dataspaceconnector-keystore.jks

ENTRYPOINT ["java", "-javaagent:./applicationinsights-agent.jar", "-javaagent:./jmx-prometheus-agent.jar=4006:./jmx_prometheus_config.yml", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]

FROM runtime AS irs-connector-consumer
COPY --from=wget jmx-prometheus-agent.jar .
COPY cd/jmx_prometheus_config.yml .
ENTRYPOINT ["java", "-javaagent:./applicationinsights-agent.jar", "-javaagent:./jmx-prometheus-agent.jar=4006:./jmx_prometheus_config.yml", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]
