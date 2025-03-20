/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.semanticshub;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.eclipse.tractusx.irs.configuration.RestTemplateConfig;
import org.eclipse.tractusx.irs.configuration.SemanticsHubConfiguration;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Semantics Hub Rest Client Implementation
 */
@Service
@Slf4j
@Profile({ "!local && !test" })
class SemanticsHubClientImpl implements SemanticsHubClient {

    public static final String LOCAL_MODEL_TYPE_BAMM = "BAMM";
    public static final String LOCAL_MODEL_TYPE_SAMM = "SAMM";
    public static final String LOCAL_MODEL_STATUS = "PROVIDED";
    private static final String PLACEHOLDER_URN = "urn";
    private final SemanticsHubConfiguration config;
    private final RestTemplate restTemplate;

    /* package */ SemanticsHubClientImpl(
            @Qualifier(RestTemplateConfig.SEMHUB_REST_TEMPLATE) final RestTemplate restTemplate,
            final SemanticsHubConfiguration config) {
        this.config = config;
        this.restTemplate = restTemplate;

        if (StringUtils.isNotBlank(config.getModelJsonSchemaEndpoint())) {
            requirePlaceholder(config.getModelJsonSchemaEndpoint());
        } else if (StringUtils.isBlank(config.getLocalModelDirectory())) {
            log.warn("No Semantic Hub URL or local model directory was provided. Cannot validate submodel payloads!");
        }
    }

    private static void requirePlaceholder(final String url) {
        if (!url.contains(wrap(SemanticsHubClientImpl.PLACEHOLDER_URN))) {
            throw new IllegalStateException(
                    "Configuration value for 'semanticsHub.modelJsonSchemaEndpoint' must contain the URL placeholder '"
                            + SemanticsHubClientImpl.PLACEHOLDER_URN + "'!");
        }
    }

    private static String wrap(final String placeholderIdType) {
        return "{" + placeholderIdType + "}";
    }

    @Override
    public String getModelJsonSchema(final String urn) throws SchemaNotFoundException {
        return readFromSemanticHub(urn).or(() -> readFromFilesystem(urn))
                                       .orElseThrow(() -> new SchemaNotFoundException(
                                               "Could not load model with URN " + urn));
    }

    @Override
    public List<AspectModel> getAllAspectModels() throws SchemaNotFoundException {
        return readAllFromSemanticHub().or(this::readAllFromFilesystem).orElse(List.of());
    }

    private Optional<List<AspectModel>> readAllFromFilesystem() {
        if (StringUtils.isNotBlank(config.getLocalModelDirectory())) {
            final Path path = Paths.get(config.getLocalModelDirectory());
            try (Stream<Path> stream = Files.list(path)) {
                return Optional.of(stream.filter(file -> !Files.isDirectory(file))
                                         .map(Path::getFileName)
                                         .map(Path::toString)
                                         .map(this::getDecodedString)
                                         .map(this::createAspectModel)
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .toList());
            } catch (IOException e) {
                log.error("Could not read schema Files.", e);
            }
        }
        return Optional.empty();
    }

    private String getDecodedString(final String urnBase64) {
        try {
            return decode(urnBase64);
        } catch (IllegalArgumentException e) {
            log.error("Could not Base64 decode urn.", e);
            return urnBase64;
        }
    }

    private Optional<AspectModel> createAspectModel(final String urn) {
        log.debug("Extracting aspect information for urn: '{}'", urn);
        final Matcher matcher = Pattern.compile("^urn:[sb]amm:.*:(\\d\\.\\d\\.\\d)#(\\w+)$").matcher(urn);
        if (matcher.find()) {
            final String version = matcher.group(1);
            final String name = matcher.group(2);
            final String localModelType = urn.contains("samm") ? LOCAL_MODEL_TYPE_SAMM : LOCAL_MODEL_TYPE_BAMM;
            return Optional.of(new AspectModel(urn, version, name, localModelType, LOCAL_MODEL_STATUS));
        }
        log.warn("Could not extract aspect information from urn: '{}'", urn);
        return Optional.empty();
    }

    private Optional<List<AspectModel>> readAllFromSemanticHub() {
        log.info("Reading models from semantic hub.");
        if (StringUtils.isNotBlank(config.getUrl())) {
            int currentPage = 0;
            final List<AspectModel> aspectModelsCollection = new ArrayList<>();
            Optional<PaginatedResponse<AspectModel>> semanticHubPage;
            do {
                semanticHubPage = getSemanticHubPage(currentPage++, config.getPageSize());
                log.info("Got response from semantic hub '{}'", semanticHubPage.toString());
                aspectModelsCollection.addAll(
                        semanticHubPage.orElseThrow().toPageImpl(config.getPageSize()).getContent());
            } while (semanticHubPage.get().toPageImpl(config.getPageSize()).hasNext());

            return Optional.of(aspectModelsCollection);
        }
        return Optional.empty();
    }

    private Optional<PaginatedResponse<AspectModel>> getSemanticHubPage(final int page, final int pageSize) {
        try {
            log.info("Request semantic hub page '{}'  with size '{}' for url '{}'", page, pageSize, config.getUrl());
            final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(config.getUrl())
                                                                        .queryParam("page", page)
                                                                        .queryParam("pageSize", pageSize);
            log.info("Semantic Hub URL '{}'", uriBuilder.toUriString());
            final ParameterizedTypeReference<PaginatedResponse<AspectModel>> responseType = ParameterizedTypeReference.forType(
                    TypeUtils.parameterize(PaginatedResponse.class, AspectModel.class));
            final ResponseEntity<PaginatedResponse<AspectModel>> result = restTemplate.exchange(
                    RequestEntity.get(uriBuilder.toUriString()).build(), responseType);
            return Optional.ofNullable(result.getBody());
        } catch (RestClientException e) {
            log.error("Unable to retrieve models from semantic hub.", e);
        }
        return Optional.empty();
    }

    private Optional<String> readFromFilesystem(final String urn) {
        if (StringUtils.isNotBlank(config.getLocalModelDirectory())) {
            final Path path = Paths.get(config.getLocalModelDirectory(), normalize(urn));
            if (path.toFile().exists()) {
                try {
                    return Optional.of(Files.readString(path));
                } catch (IOException e) {
                    log.error("Unable to read schema file at path '{}'", path, e);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> readFromSemanticHub(final String urn) {
        if (StringUtils.isNotBlank(config.getModelJsonSchemaEndpoint())) {
            try {
                final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(
                        config.getModelJsonSchemaEndpoint());
                uriBuilder.uriVariables(Map.of(PLACEHOLDER_URN, urn));
                return Optional.ofNullable(restTemplate.getForObject(uriBuilder.build().toUri(), String.class));
            } catch (final RestClientException e) {
                log.error("Unable to retrieve schema from semantic hub for urn '{}'", urn, e);
            }
        }
        return Optional.empty();
    }

    private String normalize(final String urn) {
        return Base64.getEncoder()
                     .withoutPadding()
                     .encodeToString(FilenameUtils.getName(urn).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(final String urnBase64) {
        return new String(Base64.getDecoder().decode(urnBase64), StandardCharsets.UTF_8);
    }
}
