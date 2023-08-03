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
package org.eclipse.tractusx.irs.services;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.IntegrityAspect;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.Submodel;
import org.springframework.stereotype.Service;

/**
 * Data Integrity of a given data chain validator
 */
@Service
@Slf4j
public class DataIntegrityService {

    /**
     * @param itemContainer data
     * @return flag indicates if chain is valid
     */
    public boolean chainDataIntegrityIsValid(final ItemContainer itemContainer) {
        final long numberOfValidSubmodels = itemContainer.getSubmodels()
                                        .stream()
                                        .takeWhile(submodel -> submodelDataIntegrityIsValid(submodel, itemContainer.getIntegrities()))
                                        .count();

        return numberOfValidSubmodels == totalNumberOfSubmodels(itemContainer);
    }

    private boolean submodelDataIntegrityIsValid(final Submodel submodel, final Set<IntegrityAspect> integrities) {
        final IntegrityAspect.ChildData childData = integrities.stream()
                                                               .map(IntegrityAspect::getChildParts)
                                                               .flatMap(Set::stream)
                                                               .filter(findIntegrityChildPart(submodel.getCatenaXId()))
                                                               .findFirst()
                                                               .orElseThrow(); // TODO create tombstone?

        final IntegrityAspect.Reference reference = childData.getReferences()
                                                             .stream()
                                                             .filter(findReference(submodel.getAspectType()))
                                                             .findFirst()
                                                             .orElseThrow(); // TODO create tombstone?

        final String calculatedHash = calculateHashForRawSubmodelPayload(submodel.getPayload());
        log.debug("Comparing hashes data integrity of Submodel {}", submodel.getIdentification());
        return reference.getHash().equals(calculatedHash);
    }

    private int totalNumberOfSubmodels(final ItemContainer itemContainer) {
        return itemContainer.getSubmodels().size();
    }

    private String calculateHashForRawSubmodelPayload(final Map<String, Object> payload) {
        return payload + "X";
    }

    private Predicate<? super IntegrityAspect.Reference> findReference(final String aspectType) {
        return reference -> reference.getSemanticModelUrn().equals(aspectType);
    }

    private Predicate<? super IntegrityAspect.ChildData> findIntegrityChildPart(final String catenaXId) {
        return integrityChildPart -> integrityChildPart.getCatenaXId().equals(catenaXId);
    }

}
