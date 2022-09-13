/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.esr.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.UUID;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/esr")
@RequiredArgsConstructor
@Validated
class EsrController {

    private static final int UUID_SIZE = 36;
    private static final int URN_PREFIX_SIZE = 9;
    private static final int GLOBAL_ASSET_ID_SIZE = URN_PREFIX_SIZE + UUID_SIZE;
    private static final String GLOBAL_ASSET_ID_REGEX = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @Operation(operationId = "getEsrCertificateStatistics",
               summary = "Register an ESR job to retrieve ESR certificate statistics for given {globalAssetId}.",
               security = @SecurityRequirement(name = "OAuth2", scopes = "write"),
               tags = { "Environmental and Social responsibility" },
               description = "Register an ESR job to retrieve ESR certificate statistics for given {globalAssetId}.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Return EsrCertificateStatistics for the requested globalAssetId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = EsrCertificateStatistics.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-esr-job-result"))
                                         }),
                            @ApiResponse(responseCode = "400", description = "Job registration failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "error",
                                                                                        ref = "#/components/examples/error-response-400"))
                                         }),
    })
    @GetMapping("/esr-statistics/{globalAssetId}/{bomLifecycle}/{certificateName}/submodel")
    public EsrCertificateStatistics getEsrCertificateStatistics(
            @Parameter(description = "Id of global asset.", schema = @Schema(description = "Id of global asset.", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
                                                                             implementation = String.class, minLength = GLOBAL_ASSET_ID_SIZE, maxLength = GLOBAL_ASSET_ID_SIZE),
                       name = "globalAssetId", example = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0")
            @Size(min = GLOBAL_ASSET_ID_SIZE, max = GLOBAL_ASSET_ID_SIZE) @Pattern(regexp = GLOBAL_ASSET_ID_REGEX)
            @PathVariable final String globalAssetId,
            @Parameter(description = "BoM Lifecycle.", schema = @Schema(description = "BoM Lifecycle of the result tree.", implementation = BomLifecycle.class),
                       name = "bomLifecycle", example = "asBuilt")
            @PathVariable final BomLifecycle bomLifecycle,
            @Parameter(description = "Type of certificate.", schema = @Schema(description = "Type of certificate.", implementation = CertificateType.class),
                       name = "certificateName", example = "ISO14001")
            @PathVariable final CertificateType certificateName) {
        log.debug("Global asset id: {}, bomLifecycle: {}, certificateName: {}", globalAssetId, bomLifecycle, certificateName);

        final int validState = 5;
        return EsrCertificateStatistics.builder()
                .jobId(UUID.randomUUID())
                .certificateName(certificateName)
                .statistics(EsrCertificateStatistics.CertificateStatistics.builder().certificatesWithStateValid(validState).build())
                .build();
    }

}
