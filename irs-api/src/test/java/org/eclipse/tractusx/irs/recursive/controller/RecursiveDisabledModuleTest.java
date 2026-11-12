/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.recursive.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import io.restassured.http.ContentType;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.InMemoryBlobStore;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.configuration.JobConfiguration;
import org.eclipse.tractusx.irs.policystore.config.PolicyConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central", "irs.recursive.enabled=false",
                        "irs.recursive.localBpnl=", "irs.recursive.timeout.defaultJobTtl=invalid",
                        "blobstore.chainOpeningGrants.containerName=" })
@ActiveProfiles(profiles = { "test", "local" })
@Import(RecursiveDisabledModuleTest.StorageConfiguration.class)
class RecursiveDisabledModuleTest extends ControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @ParameterizedTest
    @CsvSource({ "GET,/irs/recursive/jobs", "POST,/irs/recursive/jobs", "POST,/irs/recursive/notifications",
            "GET,/irs/recursive/chain-openings/grants" })
    void shouldNotExposeRecursiveEndpoints(final String method, final String path) {
        authenticateWith(IrsRoles.ADMIN_IRS);

        given().port(port).contentType(ContentType.JSON).body("{}")
                .request(method, path).then().statusCode(NOT_FOUND.value());
    }

    @Test
    void shouldKeepIterativeIrsAndEssMappings() {
        final Set<String> paths = handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream()).collect(Collectors.toSet());

        assertThat(paths).contains("/irs/jobs", "/ess/bpn/investigations", "/ess/notification/receive-recursive")
                .noneMatch(path -> path.startsWith("/irs/recursive"));
    }

    @Test
    void shouldStillRequireAuthentication() {
        when(authenticationService.getAuthentication(any(HttpServletRequest.class)))
                .thenThrow(new BadCredentialsException("Wrong ApiKey"));

        given().port(port).get("/irs/recursive/jobs").then().statusCode(UNAUTHORIZED.value());
    }

    @Test
    void shouldExcludeRecursiveOperationsFromOpenApi() {
        final Set<String> paths = given().port(port).get("/api/api-docs")
                .then().statusCode(OK.value()).extract().jsonPath().<String, Object>getMap("paths").keySet();

        assertThat(paths).contains("/irs/jobs", "/ess/bpn/investigations")
                .noneMatch(path -> path.startsWith("/irs/recursive"));
    }

    @TestConfiguration
    static class StorageConfiguration {

        @Primary
        @Bean(JobConfiguration.JOB_BLOB_PERSISTENCE)
        public BlobPersistence jobBlobStore() {
            return new InMemoryBlobStore();
        }

        @Bean(PolicyConfiguration.POLICY_BLOB_PERSISTENCE)
        public BlobPersistence policyBlobStore() {
            return new InMemoryBlobStore();
        }
    }
}
