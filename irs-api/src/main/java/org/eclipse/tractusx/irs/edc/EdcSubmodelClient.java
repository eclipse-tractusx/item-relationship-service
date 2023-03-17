/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.edc;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.configuration.EdcConfiguration;
import org.eclipse.tractusx.irs.configuration.local.CxTestDataContainer;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.services.AsyncPollingService;
import org.eclipse.tractusx.irs.services.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.ResourceAccessException;

/**
 * Public API facade for EDC domain
 */
public interface EdcSubmodelClient {
    CompletableFuture<List<Relationship>> getRelationships(String submodelEndpointAddress,
            RelationshipAspect traversalAspectType) throws EdcClientException;

    CompletableFuture<String> getSubmodelRawPayload(String submodelEndpointAddress) throws EdcClientException;
}

/**
 * Submodel facade stub used in local environment
 */
@Service
@Profile({ "local",
           "stubtest"
})
class EdcSubmodelClientLocalStub implements EdcSubmodelClient {

    private final JsonUtil jsonUtil;
    private final SubmodelTestdataCreator testdataCreator;

    /* package */ EdcSubmodelClientLocalStub(final JsonUtil jsonUtil, final CxTestDataContainer cxTestDataContainer) {
        this.jsonUtil = jsonUtil;
        this.testdataCreator = new SubmodelTestdataCreator(cxTestDataContainer);
    }

    @Override
    public CompletableFuture<List<Relationship>> getRelationships(final String submodelEndpointAddress,
            final RelationshipAspect traversalAspectType) throws EdcClientException {
        if ("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446".equals(submodelEndpointAddress)) {
            throw new EdcClientException("Dummy Exception");
        }

        return CompletableFuture.completedFuture(
                testdataCreator.createSubmodelForId(submodelEndpointAddress, traversalAspectType.getSubmodelClazz())
                               .asRelationships());
    }

    @Override
    public CompletableFuture<String> getSubmodelRawPayload(final String submodelEndpointAddress) {
        final Map<String, Object> submodel = testdataCreator.createSubmodelForId(submodelEndpointAddress);
        return CompletableFuture.completedFuture(jsonUtil.asString(submodel));
    }
}

/**
 * Public API facade for EDC domain
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Profile({ "!local && !stubtest" })
class EdcSubmodelClientImpl implements EdcSubmodelClient {

    private final EdcConfiguration config;
    private final ContractNegotiationService contractNegotiationService;
    private final EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage;
    private final JsonUtil jsonUtil;
    private final AsyncPollingService pollingService;
    private final OutboundMeterRegistryService meterRegistryService;
    private final RetryRegistry retryRegistry;
    private final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

    @Override
    public CompletableFuture<List<Relationship>> getRelationships(final String submodelEndpointAddress,
            final RelationshipAspect traversalAspectType) throws EdcClientException {
        return execute(submodelEndpointAddress, () -> {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Get EDC Submodel task for relationships, endpoint " + submodelEndpointAddress);

            final NegotiationResponse negotiationResponse = fetchNegotiationResponse(submodelEndpointAddress);

            return startSubmodelDataRetrieval(traversalAspectType, negotiationResponse.getContractAgreementId(),
                    stopWatch);
        });
    }

    private NegotiationResponse fetchNegotiationResponse(final String submodelEndpointAddress)
            throws EdcClientException {
        final int indexOfUrn = findIndexOf(submodelEndpointAddress, config.getSubmodel().getUrnPrefix());
        final int indexOfSubModel = findIndexOf(submodelEndpointAddress, config.getSubmodel().getPath());

        if (indexOfUrn == -1 || indexOfSubModel == -1) {
            throw new EdcClientException(
                    "Cannot rewrite endpoint address, malformed format: " + submodelEndpointAddress);
        }

        final String providerConnectorUrl = submodelEndpointAddress.substring(0, indexOfUrn);
        final String target = submodelEndpointAddress.substring(indexOfUrn + 1, indexOfSubModel);
        final String decodedTarget = URLDecoder.decode(target, StandardCharsets.UTF_8);
        log.info("Starting contract negotiation with providerConnectorUrl {} and target {}", providerConnectorUrl,
                decodedTarget);
        return contractNegotiationService.negotiate(providerConnectorUrl, decodedTarget);
    }

    private CompletableFuture<List<Relationship>> startSubmodelDataRetrieval(
            final RelationshipAspect traversalAspectType, final String contractAgreementId, final StopWatch stopWatch) {

        return pollingService.<List<Relationship>>createJob()
                             .action(() -> {
                                 final Optional<String> data = retrieveSubmodelData(config.getSubmodel().getPath(),
                                         contractAgreementId, stopWatch);
                                 if (data.isPresent()) {
                                     final RelationshipSubmodel relationshipSubmodel = jsonUtil.fromString(data.get(),
                                             traversalAspectType.getSubmodelClazz());

                                     return Optional.of(relationshipSubmodel.asRelationships());
                                 }
                                 return Optional.empty();
                             })
                             .timeToLive(config.getSubmodel().getRequestTtl())
                             .description("waiting for submodel retrieval")
                             .build()
                             .schedule();

    }

    private Optional<String> retrieveSubmodelData(final String submodel, final String contractAgreementId,
            final StopWatch stopWatch) {
        log.info("Retrieving dataReference from storage for contractAgreementId {}", contractAgreementId);
        final Optional<EndpointDataReference> dataReference = endpointDataReferenceStorage.remove(contractAgreementId);

        if (dataReference.isPresent()) {
            final EndpointDataReference ref = dataReference.get();
            log.info("Retrieving data from EDC data plane with dataReference {}:{}", ref.getAuthKey(),
                    ref.getAuthCode());
            final String data = edcDataPlaneClient.getData(ref, submodel);
            stopWatch.stop();
            log.info("EDC Task '{}' took {} ms", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());

            return Optional.of(data);
        }
        return Optional.empty();
    }

    private int findIndexOf(final String endpointAddress, final String str) {
        return endpointAddress.indexOf(str);
    }

    @Override
    public CompletableFuture<String> getSubmodelRawPayload(final String submodelEndpointAddress)
            throws EdcClientException {
        return execute(submodelEndpointAddress, () -> {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("Get EDC Submodel task for raw payload, endpoint " + submodelEndpointAddress);

            final NegotiationResponse negotiationResponse = fetchNegotiationResponse(submodelEndpointAddress);
            return pollingService.<String>createJob()
                                 .action(() -> retrieveSubmodelData(config.getSubmodel().getPath(),
                                         negotiationResponse.getContractAgreementId(), stopWatch))
                                 .timeToLive(config.getSubmodel().getRequestTtl())
                                 .description("waiting for submodel retrieval")
                                 .build()
                                 .schedule();
        });
    }

    @SuppressWarnings({"PMD.AvoidRethrowingException", "PMD.AvoidCatchingGenericException"})
    private <T> T execute(final String endpointAddress, final CheckedSupplier<T> supplier) throws EdcClientException {
        if (!urlValidator.isValid(endpointAddress)) {
            throw new IllegalArgumentException(String.format("Malformed endpoint address '%s'", endpointAddress));
        }
        final String host = URI.create(endpointAddress).getHost();
        final Retry retry = retryRegistry.retry(host, "default");
        try {
            return Retry.decorateCallable(retry, () -> {
                try {
                    return supplier.get();
                } catch (ResourceAccessException e) {
                    if (e.getCause() instanceof SocketTimeoutException) {
                        meterRegistryService.incrementSubmodelTimeoutCounter(endpointAddress);
                    }
                    throw e;
                }
            }).call();
        } catch (EdcClientException e) {
            throw e;
        } catch (Exception e) {
            throw new EdcClientException(e);
        }
    }

    /**
     * Functional interface for a supplier that may throw a checked exception.
     * @param <T> the returned type
     */
    private interface CheckedSupplier<T> {
        T get() throws EdcClientException;
    }
}
