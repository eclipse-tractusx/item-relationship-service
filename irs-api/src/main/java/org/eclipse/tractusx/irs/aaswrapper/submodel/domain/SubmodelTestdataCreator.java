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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to create Submodel Testdata
 * As AASWrapper is not deployed, we are using this class to Stub responses
 */
class SubmodelTestdataCreator {
    private final List<AssemblyPartRelationship> testData;

    /* package */ SubmodelTestdataCreator() {
        testData = new ArrayList<>();
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6",
                List.of("urn:uuid:09b48bcc-8993-4379-a14d-a7740e1c61d4", "urn:uuid:5ce49656-5156-4c8a-b93e-19422a49c0bc",
                        "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d")));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:09b48bcc-8993-4379-a14d-a7740e1c61d4",
                List.of("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446", "urn:uuid:8c1407e8-a911-4236-ac0a-03c48e6cbf19",
                        "urn:uuid:ea724f73-cb93-4b7b-b92f-d97280ff888b")));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:5ce49656-5156-4c8a-b93e-19422a49c0bc", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:8c1407e8-a911-4236-ac0a-03c48e6cbf19", List.of()));
        testData.add(getDummyAssemblyPartRelationshipWithChildren("urn:uuid:ea724f73-cb93-4b7b-b92f-d97280ff888b", List.of()));
    }

    /* package */ AssemblyPartRelationship getDummyAssemblyPartRelationshipWithChildren(final String catenaXId,
            final List<String> childIds) {
        final AssemblyPartRelationship assemblyPartRelationship = new AssemblyPartRelationship();
        assemblyPartRelationship.setCatenaXId(catenaXId);

        final Set<ChildData> childData = new HashSet<>();
        childIds.forEach(childId -> {
            final Quantity quantity = new Quantity();
            quantity.setQuantityNumber(1d);
            final Quantity.MeasurementUnit measurementUnit = new Quantity.MeasurementUnit();
            measurementUnit.setDatatypeURI("urn:bamm:io.openmanufacturing:meta-model:1.0.0#sth");
            measurementUnit.setLexicalValue("sth");
            quantity.setMeasurementUnit(measurementUnit);
            final ChildData child = new ChildData(ZonedDateTime.now(ZoneOffset.UTC),
                    quantity, ZonedDateTime.now(ZoneOffset.UTC),
                    LifecycleContextCharacteristic.ASBUILT, childId);
            childData.add(child);
        });
        assemblyPartRelationship.setChildParts(childData);

        return assemblyPartRelationship;
    }

    public AssemblyPartRelationship createDummyAssemblyPartRelationshipForId(final String catenaXId) {
        final List<AssemblyPartRelationship> collect = testData.stream()
                                                               .filter(assemblyPartRelationship -> assemblyPartRelationship.getCatenaXId()
                                                                                                                           .equals(catenaXId))
                                                               .collect(Collectors.toList());
        final AssemblyPartRelationship other = new AssemblyPartRelationship();
        other.setCatenaXId(catenaXId);
        other.setChildParts(Set.of());

        return collect.stream().findFirst().orElse(other);
    }

    public String createDummySerialPartTypizationString() {
        return "{\"localIdentifiers\":["
                + "{\"value\":\"BPNL00000003AYRE\",\"key\":\"ManufacturerID\"},"
                + "{\"value\":\"8840838-04\",\"key\":\"ManufacturerPartID\"},"
                + "{\"value\":\"NO-397646649734958738335866\",\"key\":\"PartInstanceID\"}],"
                + "\"manufacturingInformation\":{\"date\":\"2022-02-04T14:48:54\",\"country\":\"DEU\"},"
                + "\"catenaXId\":\"urn:uuid:5f479670-0b9a-475f-88e9-30f3558eb5aa\","
                + "\"partTypeInformation\":{\"manufacturerPartID\":\"8840838-04\",\"customerPartId\":\"8840838-04\","
                + "\"classification\":\"component\",\"nameAtManufacturer\":\"HVMODUL\",\"nameAtCustomer\":\"HVMODUL\"}}";
    }
}
