# Copyright (c) 2021,2022,2023
#       2022: ZF Friedrichshafen AG
#       2022: ISTOS GmbH
#       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
#       2022,2023: BOSCH AG
# Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0. *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# * SPDX-License-Identifier: Apache-2.0

# Dependencies
FROM maven:3-openjdk-17-slim AS maven
ARG BUILD_TARGET=irs-api

WORKDIR /build

COPY .config .config
COPY .mvn .mvn
COPY pom.xml .

COPY irs-ess irs-ess
COPY irs-policy-store irs-policy-store
COPY irs-integration-tests irs-integration-tests
COPY irs-api irs-api
COPY irs-common irs-common
COPY irs-edc-client irs-edc-client
COPY irs-registry-client irs-registry-client
COPY irs-models irs-models
COPY irs-parent-spring-boot irs-parent-spring-boot
COPY irs-testing irs-testing
COPY irs-report-aggregate irs-report-aggregate
COPY irs-cucumber-tests irs-cucumber-tests
COPY docs docs
COPY irs-load-tests irs-load-tests

# the --mount option requires BuildKit.
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -pl :$BUILD_TARGET -am -DskipTests


# Copy the jar and build image
FROM eclipse-temurin:20-jre-alpine AS irs-api

RUN apk upgrade --no-cache libssl3 libcrypto3

ARG UID=10000
ARG GID=3000

WORKDIR /app

COPY --chmod=755 --from=maven /build/irs-api/target/irs-api-*-exec.jar app.jar

USER ${UID}:${GID}

ENTRYPOINT ["java", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]

HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:4004/actuator/health || exit 1
